package org.chenile.core.external;

import org.chenile.core.annotation.ExternalApi;
import org.chenile.core.context.ChenileExchange;

public record ExternalApiMetadata(boolean enabled, String system, String operation) {
    public static ExternalApiMetadata from(ChenileExchange exchange) {
        ExternalApi annotation = exchange.getExtensionByAnnotation(ExternalApi.class);
        if (annotation == null || !annotation.enabled()) {
            return null;
        }
        String operation = annotation.operation().isBlank()
                ? exchange.getOperationDefinition().getName()
                : annotation.operation();
        return new ExternalApiMetadata(true, annotation.system(), operation);
    }
}
