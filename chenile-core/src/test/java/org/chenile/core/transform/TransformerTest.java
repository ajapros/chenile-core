package org.chenile.core.transform;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransformerTest {
    public record Payload(String customerId) { }

    @Test void transformsDecodedListUsingTheGenericBodyParameterType() {
        var bodyType = new TypeReference<List<Payload>>() { };
        var bodyParameter = new ParamDefinition();
        bodyParameter.setType(HttpBindingType.BODY);
        bodyParameter.setParamType(bodyType.getType());

        var operation = new OperationDefinition();
        operation.setInput(List.class);
        operation.setParams(List.of(bodyParameter));

        var exchange = new ChenileExchange();
        exchange.setServiceDefinition(new ChenileServiceDefinition());
        exchange.setOperationDefinition(operation);
        exchange.setBody(List.of(Map.of("customerId", "customer-7")));

        new TransformationClassSelector().doPreProcessing(exchange);
        assertEquals(bodyType.getType(), exchange.getBodyType().getType());

        new Transformer().doPreProcessing(exchange);

        assertEquals(List.of(new Payload("customer-7")), exchange.getBody());
    }

    @Test void keepsExplicitOperationInputWhenBodyParameterUsesABroaderType() {
        var bodyParameter = new ParamDefinition();
        bodyParameter.setType(HttpBindingType.BODY);
        bodyParameter.setParamType(BroadBody.class);

        var operation = new OperationDefinition();
        operation.setInput(Payload.class);
        operation.setParams(List.of(bodyParameter));

        var exchange = new ChenileExchange();
        exchange.setServiceDefinition(new ChenileServiceDefinition());
        exchange.setOperationDefinition(operation);

        new TransformationClassSelector().doPreProcessing(exchange);

        assertEquals(Payload.class, exchange.getBodyType().getType());
    }

    public abstract static class BroadBody { }

    @Test void preservesAlreadyTypedListElements() {
        var payload = new Payload("customer-7");
        var body = List.of(payload);
        var exchange = new ChenileExchange();
        exchange.setBody(body);
        exchange.setBodyType(new TypeReference<List<Payload>>() { });

        new Transformer().doPreProcessing(exchange);

        assertSame(body, exchange.getBody());
        assertSame(payload, ((List<?>) exchange.getBody()).get(0));
    }

    @Test void preservesHeterogeneousObjectsInListOfObjects() {
        var payload = new Payload("customer-7");
        var otherPayload = new OtherPayload(7);
        List<Object> body = List.of(payload, otherPayload, "raw-value");
        var exchange = new ChenileExchange();
        exchange.setBody(body);
        exchange.setBodyType(new TypeReference<List<Object>>() { });

        new Transformer().doPreProcessing(exchange);

        assertSame(body, exchange.getBody());
        assertSame(payload, ((List<?>) exchange.getBody()).get(0));
        assertSame(otherPayload, ((List<?>) exchange.getBody()).get(1));
    }

    public record OtherPayload(int value) { }

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
