package org.chenile.trigger;

import java.util.LinkedHashMap;
import java.util.Map;
import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ContextContainer;
import org.chenile.trigger.model.TriggerInput;

/**
 * Validation and header rules shared by every {@link TriggerService} implementation. Stateless:
 * each implementation holds its own instance.
 */
public class TriggerRequests {
  public static final String INVALID_TRIGGER = "INVALID_TRIGGER";

  public void validate(TriggerInput input) {
    require(input != null, "Trigger input is required");
    require(input.eventId != null && !input.eventId.isBlank(), "Trigger eventId is required");
  }

  /**
   * The headers the event is raised with: the caller's {@code x-} context, then the input's
   * headers, with the tenant reconciled against the caller and the source set. Any incoming trigger
   * id is dropped so that a caller cannot choose or spoof the accepted trigger.
   */
  public Map<String, String> headers(TriggerInput input) {
    var headers = new LinkedHashMap<String, String>();
    ContextContainer context = ContextContainer.getInstance();
    context
        .toMap()
        .forEach(
            (key, value) -> {
              if (key.startsWith("x-")) headers.put(key, value);
            });
    if (input.headers != null)
      input.headers.forEach(
          (key, value) -> {
            require(key != null && key.startsWith("x-"), "Trigger headers must start with x-");
            headers.put(key, value);
          });
    String tenant = headers.get(TriggerHeaders.TENANT_ID);
    String callerTenant = context.get(TriggerHeaders.TENANT_ID);
    if (callerTenant != null && !callerTenant.isBlank()) {
      require(
          tenant == null || callerTenant.equals(tenant),
          "Trigger tenant must match caller context");
      tenant = callerTenant;
    }
    headers.put(TriggerHeaders.TENANT_ID, tenant == null ? "" : tenant);
    headers.put(TriggerHeaders.EVENT_SOURCE, source(input));
    headers.remove(TriggerHeaders.TRIGGER_ID);
    return headers;
  }

  public String source(TriggerInput input) {
    return input.source == null || input.source.isBlank() ? TriggerSources.EXTERNAL : input.source;
  }

  public String tenant(Map<String, String> headers) {
    String tenant = headers.get(TriggerHeaders.TENANT_ID);
    return tenant == null ? "" : tenant;
  }

  private void require(boolean condition, String message) {
    if (!condition) throw new BadRequestException(INVALID_TRIGGER, message);
  }
}
