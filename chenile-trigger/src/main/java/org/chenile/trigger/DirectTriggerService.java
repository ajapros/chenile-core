package org.chenile.trigger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.chenile.base.exception.NotFoundException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.event.EventProcessor;
import org.chenile.trigger.model.TriggerInput;
import org.chenile.trigger.model.TriggerResult;
import org.chenile.trigger.model.TriggerStatus;

/**
 * Raises the event at once, in the caller's thread, and returns when every subscriber has
 * finished. A failure is rethrown to the caller and the idempotency key is released so that the
 * caller can retry.
 *
 * <p>Deduplication and status are kept in memory, per node, for a limited time: this is the
 * default for applications without process management. Process management replaces it with a
 * durable, outbox-backed implementation that deduplicates across nodes.
 */
public class DirectTriggerService implements TriggerService {
  public static final String RUNNING = "RUNNING";
  public static final String DONE = "DONE";
  public static final String FAILED = "FAILED";
  public static final String TRIGGER_NOT_FOUND = "TRIGGER_NOT_FOUND";

  private record Tracked(String tenant, TriggerStatus status) {}

  private final EventProcessor events;
  private final TriggerRequests requests = new TriggerRequests();
  private final Clock clock;
  private final RecentEntries<List<String>, String> acceptedKeys;
  private final RecentEntries<String, Tracked> statuses;

  public DirectTriggerService(EventProcessor events, Duration retention, int maxEntries) {
    this(events, retention, maxEntries, Clock.systemUTC());
  }

  public DirectTriggerService(
      EventProcessor events, Duration retention, int maxEntries, Clock clock) {
    this.events = events;
    this.clock = clock;
    acceptedKeys = new RecentEntries<>(retention, maxEntries, clock);
    statuses = new RecentEntries<>(retention, maxEntries, clock);
  }

  @Override
  public TriggerResult trigger(TriggerInput input) {
    requests.validate(input);
    Map<String, String> headers = requests.headers(input);
    String tenant = requests.tenant(headers);
    String newId = UUID.randomUUID().toString();
    List<String> key =
        input.idempotencyKey == null || input.idempotencyKey.isBlank()
            ? null
            : List.of(tenant, input.eventId, input.idempotencyKey);
    if (key != null) {
      String acceptedId = acceptedKeys.putIfAbsent(key, k -> newId);
      if (!acceptedId.equals(newId)) return new TriggerResult(acceptedId);
    }
    headers.put(TriggerHeaders.TRIGGER_ID, newId);
    Instant triggerTime = input.triggerTime == null ? clock.instant() : input.triggerTime;
    Instant startedAt = clock.instant();
    record(tenant, newId, input, triggerTime, startedAt, RUNNING, null, null);
    try {
      raise(newId, input.eventId, input.payload, headers);
    } catch (RuntimeException failure) {
      if (key != null) acceptedKeys.remove(key);
      record(tenant, newId, input, triggerTime, startedAt, FAILED, clock.instant(), message(failure));
      throw failure;
    }
    record(tenant, newId, input, triggerTime, startedAt, DONE, clock.instant(), null);
    return new TriggerResult(newId);
  }

  @Override
  public TriggerStatus status(String triggerId) {
    String tenant = ContextContainer.getInstance().getTenant();
    Tracked tracked = triggerId == null ? null : statuses.get(triggerId);
    if (tracked == null || !Objects.equals(tracked.tenant(), tenant == null ? "" : tenant))
      throw new NotFoundException(TRIGGER_NOT_FOUND, "Trigger not found in this tenant");
    return tracked.status();
  }

  /** Raises the event; nobody listening or a failed subscriber is an error. */
  private void raise(String triggerId, String eventId, Object payload, Map<String, String> headers) {
    // The Chenile pipeline clears the context when the subscriber returns; the caller keeps its own.
    ContextContainer context = ContextContainer.getInstance();
    ContextContainer.ContextSnapshot snapshot = context.snapshot();
    List<ChenileExchange> exchanges;
    try {
      exchanges = events.handleEvent(eventId, payload, headers);
    } catch (RuntimeException failure) {
      throw new IllegalStateException("Trigger subscriber failed for " + triggerId, failure);
    } finally {
      context.restore(snapshot);
    }
    if (exchanges.isEmpty())
      throw new IllegalStateException("No event subscriber configured for " + eventId);
    for (ChenileExchange exchange : exchanges)
      if (exchange.getException() != null)
        throw new IllegalStateException(
            "Trigger subscriber failed for " + triggerId, exchange.getException());
  }

  private void record(
      String tenant,
      String id,
      TriggerInput input,
      Instant triggerTime,
      Instant startedAt,
      String status,
      Instant finishedAt,
      String error) {
    statuses.put(
        id,
        new Tracked(
            tenant,
            new TriggerStatus(
                id,
                input.eventId,
                status,
                1,
                triggerTime,
                startedAt,
                startedAt,
                finishedAt,
                error,
                requests.source(input),
                input.triggerContext == null ? Map.of() : input.triggerContext,
                input.payload)));
  }

  /** The subscriber's own message: Chenile wraps it in a ServerException, which we wrap again. */
  private String message(Throwable failure) {
    Throwable cause = failure;
    while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
    String text = cause.getMessage();
    return text == null ? cause.getClass().getSimpleName() : text;
  }

  /**
   * A thread-safe map that forgets entries after a time to live and keeps at most a fixed number of
   * them, dropping the oldest first. Holds the accepted keys and the statuses.
   */
  private static class RecentEntries<K, V> {
    private record Entry<V>(V value, Instant expiresAt) {}

    private final LinkedHashMap<K, Entry<V>> entries = new LinkedHashMap<>();
    private final Duration timeToLive;
    private final int maxEntries;
    private final Clock clock;

    RecentEntries(Duration timeToLive, int maxEntries, Clock clock) {
      if (timeToLive == null || timeToLive.isNegative() || timeToLive.isZero())
        throw new IllegalArgumentException("timeToLive must be positive");
      if (maxEntries < 1) throw new IllegalArgumentException("maxEntries must be at least 1");
      this.timeToLive = timeToLive;
      this.maxEntries = maxEntries;
      this.clock = clock;
    }

    /** Returns the live value for the key, or stores the computed one and returns it. */
    synchronized V putIfAbsent(K key, Function<K, V> value) {
      V existing = get(key);
      if (existing != null) return existing;
      V created = value.apply(key);
      put(key, created);
      return created;
    }

    synchronized V get(K key) {
      Entry<V> entry = entries.get(key);
      if (entry == null) return null;
      if (!entry.expiresAt().isAfter(clock.instant())) {
        entries.remove(key);
        return null;
      }
      return entry.value();
    }

    synchronized void put(K key, V value) {
      entries.remove(key);
      entries.put(key, new Entry<>(value, clock.instant().plus(timeToLive)));
      evict();
    }

    synchronized void remove(K key) {
      entries.remove(key);
    }

    private void evict() {
      Instant now = clock.instant();
      Iterator<Map.Entry<K, Entry<V>>> oldestFirst = entries.entrySet().iterator();
      while (oldestFirst.hasNext()) {
        Map.Entry<K, Entry<V>> oldest = oldestFirst.next();
        if (entries.size() > maxEntries || !oldest.getValue().expiresAt().isAfter(now))
          oldestFirst.remove();
        else break;
      }
    }
  }
}
