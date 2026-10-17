package org.chenile.core.transform;

import org.chenile.core.context.ChenileExchange;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransformerTest {
    public record Payload(String customerId) { }

    @Test void transformsRawAndDecodedJsonUsingTheSelectedTypeAndPreservesTypedBodies() {
        var transformer = new Transformer();
        var mapper = JsonMapper.builder().build();
        String raw = "{\"customerId\":\"customer-7\"}";
        for (Object body : List.of(raw, Map.of("customerId", "customer-7"), mapper.readTree(raw))) {
            var exchange = new ChenileExchange();
            exchange.setBody(body);
            exchange.setBodyType(new TypeReference<Payload>() { });
            transformer.doPreProcessing(exchange);
            assertEquals(new Payload("customer-7"), exchange.getBody());
        }

        var exchange = new ChenileExchange();
        exchange.setBody(List.of(Map.of("customerId", "customer-7")));
        exchange.setBodyType(new TypeReference<List<Payload>>() { });
        transformer.doPreProcessing(exchange);
        assertEquals(List.of(new Payload("customer-7")), exchange.getBody());

        Payload typed = new Payload("customer-7");
        exchange.setBody(typed);
        exchange.setBodyType(new TypeReference<Payload>() { });
        transformer.doPreProcessing(exchange);
        assertSame(typed, exchange.getBody());
    }
}
