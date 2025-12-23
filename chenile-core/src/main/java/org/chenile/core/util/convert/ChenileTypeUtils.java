package org.chenile.core.util.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class ChenileTypeUtils {
    private static Logger logger = LoggerFactory.getLogger(ChenileTypeUtils.class);
    private ChenileTypeUtils() {
    }

    public static ParameterizedTypeReference<?> makeParameterizedTypeReference(String s) {
        JavaType javaType;
        try {
            javaType = ChenileGenericTypeParser.parse(s);
        }catch(Exception e){
            logger.warn("Cannot make a Parameterized type reference from provided string {}", s);
            return null;
        }
        logger.info("JavaType: Converting to Parameterized type reference {}", javaType);
        return ChenileTypeUtils.toParameterizedTypeReference(javaType);
    }

    public static TypeReference<?> makeTypeReference(String s) throws ClassNotFoundException {
        JavaType javaType = ChenileGenericTypeParser.parse(s);
        logger.info("JavaType: {}", javaType);
        return ChenileTypeUtils.toTypeReference(javaType);
    }

    public static ParameterizedTypeReference<?> toParameterizedTypeReference(JavaType javaType) {
        return ParameterizedTypeReference.forType(toReflectType(javaType));
    }

    public static TypeReference<?> toTypeReference(JavaType javaType) {
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return toReflectType(javaType);
            }
        };
    }

    private static Type toReflectType(JavaType javaType) {
        if (javaType.hasGenericTypes()) {
            return new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return javaType.getBindings()
                            .getTypeParameters()
                            .stream()
                            .map(ChenileTypeUtils::toReflectType)
                            .toArray(Type[]::new);
                }

                @Override
                public Type getRawType() {
                    return javaType.getRawClass();
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }

                @Override
                public String toString() {
                    // force PRE-style output
                    StringBuilder sb = new StringBuilder(getRawType().getTypeName());
                    sb.append("<");
                    sb.append(
                            Arrays.stream(getActualTypeArguments())
                                    .map(Type::getTypeName)
                                    .reduce((a, b) -> a + ", " + b).orElse("")
                    );
                    sb.append(">");
                    return sb.toString();
                }
            };
        }
        return javaType.getRawClass();
    }
}
