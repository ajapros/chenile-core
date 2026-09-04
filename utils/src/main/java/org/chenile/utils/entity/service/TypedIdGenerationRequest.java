package org.chenile.utils.entity.service;

import org.chenile.core.context.ContextContainer;

/** Context supplied to a typed application ID generator. */
public record TypedIdGenerationRequest(String prefix, Class<?> entityType,
                                       ContextContainer contextContainer) {
}
