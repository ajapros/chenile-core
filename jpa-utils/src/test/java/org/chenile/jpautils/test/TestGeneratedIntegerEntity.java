package org.chenile.jpautils.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaGeneratedIntegerEntity;
import org.chenile.jpautils.entity.ChenileGeneratedId;

@Entity
@Table(name = "test_generated_integer_entity")
@ChenileGeneratedId(strategy = "integerId")
public class TestGeneratedIntegerEntity extends BaseJpaGeneratedIntegerEntity {
}
