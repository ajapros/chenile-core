package org.chenile.mcp.model;

import tools.jackson.core.type.TypeReference;

import java.util.Map;

public record ChenilePolymorphVariant(
        String nameSuffix,
        String description,
        Map<String, TypeReference<?>> parameterTypes,
        Map<String, Object> fixedParameterValues) {

    public ChenilePolymorphVariant {
        parameterTypes = parameterTypes == null ? Map.of() : Map.copyOf(parameterTypes);
        fixedParameterValues = fixedParameterValues == null ? Map.of() : Map.copyOf(fixedParameterValues);
    }
}
