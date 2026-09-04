package org.chenile.jpautils.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Non-workflow JPA entity base with a database-generated Long primary key. */
@MappedSuperclass
public class BaseJpaLongEntity extends AbstractJpaNumericEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Long id;

    @Override public Long getId() { return id; }

    @Override public void setId(Long id) { this.id = id; }
}
