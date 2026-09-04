package org.chenile.jpautils.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Non-workflow JPA entity base with a database-generated Integer primary key. */
@MappedSuperclass
public class BaseJpaIntegerEntity extends AbstractJpaNumericEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Integer id;

    @Override public Integer getId() { return id; }

    @Override public void setId(Integer id) { this.id = id; }
}
