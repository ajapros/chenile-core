# Chenile response headers

## Service-provided response headers

A service result can implement `org.chenile.base.response.ResponseHeadersAware` to provide
response headers. The interface lives in `chenile-base`; service APIs need no dependency on
`chenile-http`, servlet response objects, or Spring `ResponseEntity`.

```java
public record CreatedItem(String id) implements ResponseHeadersAware {
    @Override
    @JsonIgnore
    public Map<String, List<String>> getResponseHeaders() {
        return Map.of("Location", List.of("/items/" + id));
    }
}
```

The service returns `CreatedItem` as usual. Chenile copies its headers before wrapping the result
in `GenericResponse`. The JSON payload remains `{"id":"..."}`; `responseHeaders` is excluded
from JSON by the interface's `@JsonIgnore` annotation. Annotating the overriding method as well
makes the intent explicit. Ordinary result objects continue to work unchanged.

Set a fixed HTTP status through existing operation metadata. For an asynchronous acknowledgement,
use `@ChenileResponseCodes(success = 202, warning = 202)` on the controller operation, or configure
the corresponding success/warning status in the JSON operation definition. Both the HTTP status
and `GenericResponse.code` then reflect that configuration. Controllers simply call `process(...)`.

`ChenileExchange` keeps response headers separate from incoming headers. Interceptors can call
`setResponseHeader(name, value)` to replace a header or `addResponseHeader(name, value)` to append.
Names are case insensitive; values retain their order. `getResponseHeaders()` returns an immutable
snapshot. Headers provided by the result are appended to any headers already set by interceptors.

Both annotated controllers (`ControllerSupport`) and configured endpoints (`HttpEntryPoint`) emit
the same metadata and preserve repeated values, including warnings. Request headers are never
implicitly echoed. A failure discards success headers, including a previously selected `Location`.
Invalid names, control characters in values, and transport-managed headers cause an error rather
than being written. The adapter owns Content-Type, Content-Length, Transfer-Encoding, Connection,
Keep-Alive, Proxy-Authenticate, Proxy-Authorization, TE, Trailer, and Upgrade.

Use standard names for standard headers (`Location`, `ETag`, `Retry-After`). Custom Chenile headers
use the `x-` prefix. Response headers do not enter the request context or propagate automatically
into nested calls. Direct Java callers keep the same result type; asynchronous adapters do not
turn HTTP response metadata into outgoing event headers. Transparent forwarding of remote-proxy
response headers is outside this contract.

The trigger acknowledgement is a concrete example: `TriggerResult` exposes
`Location: /trigger/<outbox-id>`, and the trigger operation configures HTTP 202. Its controller
contains no special response-header or status-rewriting logic. Header publication does not change
the guarantee that durable acceptance commits before the acknowledgement is returned.
