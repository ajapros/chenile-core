package org.chenile.jpautils.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Non-workflow JPA entity base with a Long key supplied by a named typed generator. */
@MappedSuperclass
public class BaseJpaGeneratedLongEntity extends AbstractJpaGeneratedNumericEntity<Long> {
    @Id @Column(name = "id") protected Long id;

    @Override public Long getId() { return id; }

    @Override public void setId(Long id) { this.id = id; }

    @Override protected Class<Long> idType() { return Long.class; }
}
