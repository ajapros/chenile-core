package org.chenile.trigger.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.chenile.trigger.TriggerSources;

public class TriggerInput {
  /** Optional retry key: a second trigger with the same tenant, event and key is ignored. */
  public String idempotencyKey;

  public String eventId;

  /** Where the trigger came from; see {@link TriggerSources}. */
  public String source = TriggerSources.EXTERNAL;

  public Instant triggerTime = Instant.now();
  public Map<String, String> headers = new LinkedHashMap<>();

  /** Source-specific metadata, kept with the trigger's status. */
  public Map<String, Object> triggerContext = new LinkedHashMap<>();

  public Object payload;
}
