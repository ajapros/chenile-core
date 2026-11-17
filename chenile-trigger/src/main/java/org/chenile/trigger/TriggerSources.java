package org.chenile.trigger;

/** Values of {@link TriggerHeaders#EVENT_SOURCE}: where a Chenile event came from. */
public final class TriggerSources {
  public static final String CRON = "CRON";
  public static final String FILE = "FILE";
  public static final String PUBSUB = "PUBSUB";
  public static final String API = "API";
  /** Code that calls {@link TriggerService} directly without naming a source. */
  public static final String EXTERNAL = "EXTERNAL";

  private TriggerSources() {}
}
