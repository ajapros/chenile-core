package org.chenile.trigger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import org.chenile.base.response.ResponseHeadersAware;

/** The accepted trigger. Its Location header points at the trigger's status. */
public record TriggerResult(String triggerId) implements ResponseHeadersAware {
  @Override
  @JsonIgnore
  public Map<String, List<String>> getResponseHeaders() {
    return Map.of("Location", List.of("/trigger/" + triggerId));
  }
}
