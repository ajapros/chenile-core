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
        String requestId = contextContainer.getRequestId();

        //In case child nested jpa entities context container values are null
        if(requestId== null || requestId.isBlank()){
            requestId = String.valueOf(UUID.randomUUID());
        }
        return String.format("%s-%s-%0,4d", prefix, requestId, obtainCounter(prefix));
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
}