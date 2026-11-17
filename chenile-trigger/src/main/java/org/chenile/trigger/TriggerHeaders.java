package org.chenile.trigger;

import org.chenile.core.context.HeaderUtils;

/**
 * Headers that every Chenile event carries, whichever source raised it. A subscriber binds them
 * as HEADER params. Only the headers that make sense for the source are set.
 */
public final class TriggerHeaders {
  /** CRON, FILE, PUBSUB, API or EXTERNAL; see {@link TriggerSources}. */
  public static final String EVENT_SOURCE = "x-chenile-event-source";
  /** The accepted trigger. Set by the trigger service; callers cannot choose it. */
  public static final String TRIGGER_ID = "x-chenile-trigger-id";
  public static final String TENANT_ID = HeaderUtils.TENANT_ID_KEY;

  public static final String SCHEDULE_ID = "x-chenile-schedule-id";
  public static final String SCHEDULED_FIRE_TIME = "x-chenile-scheduled-fire-time";
  public static final String FIRE_TIME = "x-chenile-fire-time";

  public static final String FILE_WATCH_ID = "x-chenile-file-watch-id";
  public static final String FILE_NAME = "x-chenile-file-name";
  public static final String RECORD_INDEX = "x-chenile-record-index";

  public static final String TOPIC = "x-chenile-topic";

  private TriggerHeaders() {}
}
