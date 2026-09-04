package org.chenile.jpautils.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import org.chenile.utils.entity.service.TypedIdGenerator;

/** Shared generated-ID callback for additive non-workflow numeric JPA entities. */
@MappedSuperclass
public abstract class AbstractJpaGeneratedNumericEntity<ID extends Number> extends AbstractJpaNumericEntity<ID> {
    @PrePersist
    protected void generateIdIfRequired() {
        if (getId() == null) {
            setId(TypedIdGenerator.generateId(strategyName(), idType(), getClass().getSimpleName(), getClass()));
        }
    }

    protected abstract Class<ID> idType();

    private String strategyName() {
        ChenileGeneratedId generatedId = getClass().getAnnotation(ChenileGeneratedId.class);
        if (generatedId == null || generatedId.strategy().isBlank()) {
            throw new IllegalStateException(getClass().getName()
                    + " must declare @ChenileGeneratedId to use a generated numeric JPA entity");
        }
        return generatedId.strategy();
    }
}
