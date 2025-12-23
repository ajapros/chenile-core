package org.chenile.core.transform;

import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;


/**
 * Performs a transformation from JSON to the {@link ChenileExchange#getBodyType()} mentioned in the
 * {@link ChenileExchange}
 *
 * @author Raja Shankar Kolluru
 */
public class Transformer extends BaseChenileInterceptor {

    private final ObjectMapper om;

    public Transformer() {


        om = JsonMapper.builder()
                //.registerSubtypes(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }

    @Override
    public void doPreProcessing(ChenileExchange exchange) {
        TypeReference<?> targetType = exchange.getBodyType();
        Object body = exchange.getBody();
        if (targetType == null || body == null) return;

        //if (body.getClass().isAssignableFrom(targetType.getType())) return;
        if (!(body instanceof String)) return;
        // unset API Invocation so that it can be recomputed
        exchange.setApiInvocation(null);
        try {
            Object newBody = om.readValue((String) body, targetType);
            exchange.setBody(newBody);
        } catch (Exception e) {
            throwBadRequestException(exchange,e);
        }
    }

    private void throwBadRequestException(ChenileExchange exchange,Exception e) {
        throw new BadRequestException(ErrorCodes.TYPE_MISMATCH.getSubError(),
            new Object[]{ exchange.getServiceDefinition().getName(),
                exchange.getOperationDefinition().getName(),
                e.getMessage()});
    }

}
