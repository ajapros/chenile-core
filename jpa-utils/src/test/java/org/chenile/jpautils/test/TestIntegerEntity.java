package org.chenile.jpautils.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaIntegerEntity;

@Entity
@Table(name = "test_integer_entity")
public class TestIntegerEntity extends BaseJpaIntegerEntity {
}
