package org.chenile.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.event.EventProcessor;
import org.chenile.trigger.model.TriggerInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Retention and size limits, which need a clock the test controls. */
class DirectTriggerServiceTest {
  static class ManualClock extends Clock {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }

  private final ManualClock clock = new ManualClock();
  private final EventProcessor events = mock(EventProcessor.class);

  @BeforeEach
  void subscriberSucceeds() {
    ContextContainer.getInstance().clear();
    when(events.handleEvent(anyString(), any(), anyMap()))
        .thenReturn(List.of(new ChenileExchange()));
  }

  @Test
  void aKeyIsForgottenAfterTheRetention() {
    var service = new DirectTriggerService(events, Duration.ofHours(24), 10, clock);
    String first = service.trigger(input("k")).triggerId();
    clock.now = clock.now.plus(Duration.ofHours(23));
    assertEquals(first, service.trigger(input("k")).triggerId());
    clock.now = clock.now.plus(Duration.ofHours(1));
    assertNotEquals(first, service.trigger(input("k")).triggerId());
  }

  @Test
  void theOldestKeyIsDroppedBeyondMaxEntries() {
    var service = new DirectTriggerService(events, Duration.ofHours(1), 2, clock);
    String a = service.trigger(input("a")).triggerId();
    service.trigger(input("b"));
    String c = service.trigger(input("c")).triggerId();
    assertEquals(c, service.trigger(input("c")).triggerId());
    assertNotEquals(a, service.trigger(input("a")).triggerId());
  }

  private static TriggerInput input(String key) {
    TriggerInput input = new TriggerInput();
    input.eventId = "orderPlaced";
    input.idempotencyKey = key;
    return input;
  }
}
