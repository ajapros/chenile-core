package org.chenile.http.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.BadRequestException;
import org.chenile.base.response.ResponseHeadersAware;
import org.chenile.base.response.ResponseMessage;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ChenileExchangeBuilder;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.core.interceptors.GenericResponseBuilder;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseHeadersTest {
    public record Ack(String id) implements ResponseHeadersAware {
        @Override public Map<String, List<String>> getResponseHeaders() {
            return Map.of("Location", List.of("/items/" + id), "x-result", List.of("one", "two"));
        }
    }
    private static class Builder extends GenericResponseBuilder {
        void finish(ChenileExchange exchange) { doPostProcessing(exchange); }
    }
    private static class Controller extends ControllerSupport {
        ResponseEntity<Object> call(MockHttpServletRequest request) { return process("create", request); }
    }
    private record Reply(int status, Map<String, List<String>> headers, Object body) { }

    // Exercise the same return journey through both HTTP response implementations.
    private Reply invoke(boolean configured, Consumer<ChenileExchange> service) throws Exception {
        OperationDefinition operation = new OperationDefinition();
        operation.setUrl("/items"); operation.setSuccessHttpStatus(202);
        ChenileEntryPoint entry = new ChenileEntryPoint() {
            @Override public void execute(ChenileExchange exchange) {
                service.accept(exchange);
                new Builder().finish(exchange);
            }
        };
        var request = new MockHttpServletRequest("POST", "/items");
        request.addHeader("x-incoming", "private");
        if (configured) {
            var response = new MockHttpServletResponse();
            new HttpEntryPoint(new ChenileServiceDefinition(), operation, entry).handleRequest(request, response);
            Map<String, List<String>> headers = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            response.getHeaderNames().forEach(name -> headers.put(name, List.copyOf(response.getHeaders(name))));
            return new Reply(response.getStatus(), headers,
                    new ObjectMapper().readTree(response.getContentAsString()));
        }
        var controller = new Controller();
        controller.chenileEntryPoint = entry;
        controller.chenileExchangeBuilder = mock(ChenileExchangeBuilder.class);
        var exchange = new ChenileExchange(); exchange.setOperationDefinition(operation);
        when(controller.chenileExchangeBuilder.makeExchange(any(), eq("create"), isNull())).thenReturn(exchange);
        var response = controller.call(request);
        Map<String, List<String>> headers = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        response.getHeaders().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return new Reply(response.getStatusCode().value(), headers,
                new ObjectMapper().valueToTree(response.getBody()));
    }

    @Test void bothHttpAdaptersEmitMetadataWithoutChangingPayloadOrEchoingRequests() throws Exception {
        for (boolean configured : List.of(false, true)) {
            var reply = invoke(configured, exchange -> exchange.setResponse(new Ack("42")));
            assertEquals(202, reply.status());
            assertEquals(List.of("/items/42"), reply.headers().get("Location"));
            assertEquals(List.of("one", "two"), reply.headers().get("x-result"));
            assertFalse(reply.headers().containsKey("x-incoming"));
            var json = new ObjectMapper().valueToTree(reply.body());
            assertEquals(202, json.get("code").asInt());
            assertEquals("42", json.get("payload").get("id").asText());
            assertFalse(json.toString().contains("responseHeaders"));
            assertFalse(new tools.jackson.databind.ObjectMapper().writeValueAsString(new Ack("42"))
                    .contains("responseHeaders"));
        }
    }

    @Test void failuresDiscardSuccessHeadersAndPlainResultsStayCompatible() throws Exception {
        for (boolean configured : List.of(false, true)) {
            var failed = invoke(configured, exchange -> {
                exchange.setResponse(new Ack("42"));
                exchange.setResponseHeader("Location", "/stale");
                exchange.setException(new BadRequestException("invalid"));
            });
            assertEquals(400, failed.status());
            assertFalse(failed.headers().containsKey("Location"));
            var plain = invoke(configured, exchange -> exchange.setResponse("ordinary"));
            assertFalse(plain.headers().containsKey("Location"));
            assertEquals("ordinary", new ObjectMapper().valueToTree(plain.body()).get("payload").asText());
        }
    }

    @Test void invalidServiceHeadersBecomeFailuresAndRepeatedWarningsArePreserved() throws Exception {
        for (boolean configured : List.of(false, true)) {
            var invalid = invoke(configured, exchange -> exchange.setResponse((ResponseHeadersAware)
                    () -> Map.of("Location", List.of("/items/42\r\nx-injected: yes"))));
            assertEquals(500, invalid.status());
            assertFalse(invalid.headers().containsKey("Location"));
            var warnings = invoke(configured, exchange -> {
                exchange.setResponse("ok");
                for (String text : List.of("first", "second")) {
                    var message = new ResponseMessage(); message.setCode(202); message.setDescription(text);
                    exchange.addWarningMessage(message);
                }
            });
            assertEquals(List.of("first", "second"), warnings.headers().get("Warning"));
        }
    }

    @Test void exchangeHeadersAreValidatedCaseInsensitiveAndIsolated() {
        var exchange = new ChenileExchange();
        exchange.setResponseHeader("x-result", "one");
        exchange.addResponseHeader("X-Result", "two");
        assertEquals(List.of("one", "two"), exchange.getResponseHeaders().get("X-RESULT"));
        var snapshot = exchange.getResponseHeaders();
        exchange.setResponseHeader("X-RESULT", "three");
        assertEquals(List.of("one", "two"), snapshot.get("x-result"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.get("x-result").add("four"));
        assertThrows(IllegalArgumentException.class, () -> exchange.setResponseHeader("bad name", "x"));
        assertThrows(IllegalArgumentException.class, () -> exchange.setResponseHeader("Content-Length", "10"));
        assertThrows(IllegalArgumentException.class, () -> exchange.setResponseHeader("Location", "bad\nvalue"));
        assertTrue(new ChenileExchange(exchange).getResponseHeaders().isEmpty());
    }
}
