package org.chenile.utils.entity.service;

/** Generates identifiers whose Java type is declared explicitly by the strategy. */
public interface TypedIdGenerationStrategy<ID> {
    Class<ID> idType();

    ID generate(TypedIdGenerationRequest request);
}
