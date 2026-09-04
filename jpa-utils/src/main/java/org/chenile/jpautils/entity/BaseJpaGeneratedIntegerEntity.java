package org.chenile.jpautils.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Non-workflow JPA entity base with an Integer key supplied by a named typed generator. */
@MappedSuperclass
public class BaseJpaGeneratedIntegerEntity extends AbstractJpaGeneratedNumericEntity<Integer> {
    @Id @Column(name = "id") protected Integer id;

    @Override public Integer getId() { return id; }

    @Override public void setId(Integer id) { this.id = id; }

    @Override protected Class<Integer> idType() { return Integer.class; }
}
