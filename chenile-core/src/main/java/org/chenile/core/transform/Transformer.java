package org.chenile.core.transform;

import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

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

        Type target = targetType.getType();
        if (!(body instanceof String)
                && !(body instanceof Map<?, ?>)
                && !(body instanceof Collection<?>)
                && !(body instanceof JsonNode)) return;
        if (!(body instanceof String) && isCompatible(body, target)) return;

        try {
            Object newBody = body instanceof String text
                    ? om.readValue(text, targetType)
                    : om.convertValue(body, targetType);
            // Recompute arguments because the body object may have changed type.
            exchange.setApiInvocation(null);
            exchange.setBody(newBody);
        } catch (Exception e) {
            throwBadRequestException(exchange,e);
        }
    }

    private boolean isCompatible(Object value, Type targetType) {
        if (value == null || targetType instanceof TypeVariable<?>) return true;

        if (targetType instanceof Class<?> targetClass) {
            if (!targetClass.isPrimitive()) return targetClass.isInstance(value);
            return primitiveWrapper(targetClass).isInstance(value);
        }

        if (targetType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class<?> rawClass) || !rawClass.isInstance(value)) return false;

            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (value instanceof Iterable<?> iterable && Iterable.class.isAssignableFrom(rawClass)) {
                for (Object element : iterable) {
                    if (!isCompatible(element, arguments[0])) return false;
                }
                return true;
            }
            if (value instanceof Map<?, ?> map && Map.class.isAssignableFrom(rawClass)) {
                return map.entrySet().stream().allMatch(entry ->
                        isCompatible(entry.getKey(), arguments[0])
                                && isCompatible(entry.getValue(), arguments[1]));
            }
            return true;
        }

        if (targetType instanceof GenericArrayType arrayType) {
            if (!value.getClass().isArray()) return false;
            Type componentType = arrayType.getGenericComponentType();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (!isCompatible(java.lang.reflect.Array.get(value, i), componentType)) return false;
            }
            return true;
        }

        if (targetType instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (!isCompatible(value, upperBound)) return false;
            }
            return true;
        }

        return true;
    }

    private Class<?> primitiveWrapper(Class<?> primitiveType) {
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == char.class) return Character.class;
        return primitiveType;
    }

    private void throwBadRequestException(ChenileExchange exchange,Exception e) {
        throw new BadRequestException(ErrorCodes.TYPE_MISMATCH.getSubError(),
            new Object[]{ exchange.getServiceDefinition().getName(),
                exchange.getOperationDefinition().getName(),
                e.getMessage()});
    }

}
