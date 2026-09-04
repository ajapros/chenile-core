package org.chenile.jpautils.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaLongEntity;

@Entity
@Table(name = "test_long_entity")
public class TestLongEntity extends BaseJpaLongEntity {
}
