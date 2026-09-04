package org.chenile.utils.entity.service;

import org.chenile.core.context.ContextContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates non-String entity IDs from named typed strategies. This is deliberately separate from
 * {@link IDGenerator}, whose String-only API remains unchanged for backwards compatibility.
 */
public final class TypedIdGenerator {
    private static final Map<String, TypedIdGenerationStrategy<?>> STRATEGIES = new ConcurrentHashMap<>();

    private TypedIdGenerator() {
    }

    public static void registerStrategy(String name, TypedIdGenerationStrategy<?> strategy) {
        if (name == null || name.isBlank() || strategy == null || strategy.idType() == null) {
            throw new IllegalArgumentException("A typed ID generator needs a name, strategy, and ID type");
        }
        STRATEGIES.put(name, strategy);
    }

    public static <ID> ID generateId(String strategyName, Class<ID> idType, String prefix, Class<?> entityType) {
        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException("A typed ID generator strategy name is required");
        }
        TypedIdGenerationStrategy<?> strategy = STRATEGIES.get(strategyName);
        if (strategy == null) {
            throw new IllegalStateException("No typed ID generator strategy named '" + strategyName + "' is registered");
        }
        if (!idType.equals(strategy.idType())) {
            throw new IllegalStateException("Typed ID generator strategy '" + strategyName + "' produces "
                    + strategy.idType().getName() + " but " + idType.getName() + " is required");
        }
        Object generated = strategy.generate(new TypedIdGenerationRequest(prefix, entityType,
                ContextContainer.CONTEXT_CONTAINER));
        if (!idType.isInstance(generated)) {
            String actualType = generated == null ? "null" : generated.getClass().getName();
            throw new IllegalStateException("Typed ID generator strategy '" + strategyName + "' returned "
                    + actualType + " but " + idType.getName() + " is required");
        }
        return idType.cast(generated);
    }
}
