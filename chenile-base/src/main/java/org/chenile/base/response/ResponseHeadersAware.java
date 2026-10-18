package org.chenile.base.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

/** Optional response metadata, emitted as headers rather than serialized in the payload. */
public interface ResponseHeadersAware {
    @JsonIgnore
    Map<String, List<String>> getResponseHeaders();
}
