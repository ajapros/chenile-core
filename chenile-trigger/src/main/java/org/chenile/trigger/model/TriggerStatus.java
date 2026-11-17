package org.chenile.trigger.model;

import java.time.Instant;
import java.util.Map;

/** Status and input metadata, never the event invocation output. */
public record TriggerStatus(
    String triggerId,
    String eventName,
    String status,
    int attempt,
    Instant triggerTime,
    Instant createdAt,
    Instant startedAt,
    Instant finishedAt,
    String error,
    String source,
    Map<String, Object> triggerContext,
    Object payload) {}
