package org.chenile.http.handler;

import org.chenile.core.context.ChenileExchange;
import org.springframework.http.HttpHeaders;
import java.util.function.BiConsumer;

/** Writes response metadata consistently for controller and configured HTTP endpoints. */
final class HttpResponseHeaders {
    private HttpResponseHeaders() { }

    static void write(ChenileExchange exchange, BiConsumer<String, String> addHeader) {
        if (exchange.getException() == null) {
            exchange.getResponseHeaders().forEach((name, values) ->
                    values.forEach(value -> addHeader.accept(name, value)));
        }
        if (exchange.getResponseMessages() != null) {
            exchange.getResponseMessages().forEach(message -> {
                if (message.getDescription() != null)
                    addHeader.accept(HttpHeaders.WARNING, message.getDescription());
            });
        }
    }
}
