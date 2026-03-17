package org.chenile.mcp.model;

import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;

import java.util.List;

public interface ChenilePolymorphProvider {
    List<ChenilePolymorphVariant> getVariants(ChenileServiceDefinition serviceDefinition,
                                              OperationDefinition operationDefinition);
}
