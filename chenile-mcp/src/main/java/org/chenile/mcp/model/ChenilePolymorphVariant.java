package org.chenile.mcp.model;

import tools.jackson.core.type.TypeReference;

import java.util.Map;

public record ChenilePolymorphVariant(
        String nameSuffix,
        String description,
        Map<String, TypeReference<?>> parameterTypes,
        Map<String, String> parameterSchemas,
        Map<String, String> parameterDescriptions,
        Map<String, Object> fixedParameterValues) {

    public ChenilePolymorphVariant {
        parameterTypes = parameterTypes == null ? Map.of() : Map.copyOf(parameterTypes);
        parameterSchemas = parameterSchemas == null ? Map.of() : Map.copyOf(parameterSchemas);
        parameterDescriptions = parameterDescriptions == null ? Map.of() : Map.copyOf(parameterDescriptions);
        fixedParameterValues = fixedParameterValues == null ? Map.of() : Map.copyOf(fixedParameterValues);
        for (String parameterName : parameterTypes.keySet()) {
            if (parameterSchemas.containsKey(parameterName)) {
                throw new IllegalArgumentException("Polymorph variant cannot define both type and schema for parameter "
                        + parameterName);
            }
        }
    }
}
