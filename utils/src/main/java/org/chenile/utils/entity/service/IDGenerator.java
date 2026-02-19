package org.chenile.utils.entity.service;

import org.chenile.core.context.ContextContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generates IDs given a request context. This class is useful to generate predictable IDs for entities
 * given a request ID. This is essential for replicating systems i.e. systems that replicate the same
 * data in multiple machines by replaying the same end user request in these machines. E.g., let us say
 * that we want to generate identical Orders in a store machine and a cloud machine. The IDs of the
 * Order cannot differ between multiple machines.<br/>
 * If you use this IDGenerator, then the IDs will be the same since they all are based out of a
 * request ID that is shared between the replicating machines.<br/>
 * Class needs to be used statically since it is designed to be called by entities in their
 * JPA \@PrePersist and \@PreUpdate methods.
 */
public abstract class IDGenerator {
    private static final String ID_MAP = "__ID_MAP__";
    public static final String STRATEGY_KEY = "__ID_GENERATOR_STRATEGY__";
    private static final String DEFAULT_STRATEGY_NAME = "default";
    private static final Map<String, IdGenerationStrategy> STRATEGIES = new HashMap<>();
    private static String defaultStrategyName = DEFAULT_STRATEGY_NAME;

    static {
        STRATEGIES.put(DEFAULT_STRATEGY_NAME, new DefaultIdGenerationStrategy());
    }
    /**
     * This method generates a new unique ID for the given prefix. The ID is based out
     * of request ID. A counter is added in case multiple IDs are required for the same
     * entity.
     *
     * @param prefix Prefix that needs to be used. Typically, each entity will have its own prefix.
     * @return the ID that can be used to generate new records for the given entity.
     */
    public static String generateID(String prefix) {
        ContextContainer contextContainer = ContextContainer.CONTEXT_CONTAINER;
        String strategyName = resolveStrategyName(contextContainer);
        IdGenerationStrategy strategy = STRATEGIES.get(strategyName);
        if (strategy == null) {
            strategy = STRATEGIES.get(DEFAULT_STRATEGY_NAME);
        }
        return strategy.generate(prefix, contextContainer);
    }

    public static void registerStrategy(String name, IdGenerationStrategy strategy) {
        if (name == null || name.isBlank() || strategy == null) {
            return;
        }
        STRATEGIES.put(name, strategy);
    }

    public static void setDefaultStrategyName(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        defaultStrategyName = name;
    }

    private static String resolveStrategyName(ContextContainer contextContainer) {
        Object ext = ContextContainer.getExtension(STRATEGY_KEY);
        if (ext instanceof String && !((String) ext).isBlank()) {
            return ((String) ext).trim();
        }
        String headerValue = contextContainer.get("idGeneratorStrategy");
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue.trim();
        }
        String sysProp = System.getProperty("chenile.id.generator.strategy");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        return defaultStrategyName;
    }

    private static int obtainCounter(String prefix) {
        int counter = obtainIdMap().computeIfAbsent(prefix, (pfx) -> 0);
        obtainIdMap().put(prefix, ++counter);
        return counter;
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Integer> obtainIdMap() {
        Map<String, Integer> idMap = (Map<String, Integer>) ContextContainer.getExtension(ID_MAP);
        if (idMap == null) {
            idMap = new HashMap<>();
            ContextContainer.putExtension(ID_MAP,idMap);
        }
        return idMap;
    }

    public interface IdGenerationStrategy {
        String generate(String prefix, ContextContainer contextContainer);
    }

    static class DefaultIdGenerationStrategy implements IdGenerationStrategy {
        @Override
        public String generate(String prefix, ContextContainer contextContainer) {
            String requestId = contextContainer.getRequestId();
            // In case child nested JPA entities context container values are null
            if (requestId == null || requestId.isBlank()) {
                requestId = String.valueOf(UUID.randomUUID());
            }
            return String.format("%s-%s-%0,4d", prefix, requestId, obtainCounter(prefix));
        }
    }
}
