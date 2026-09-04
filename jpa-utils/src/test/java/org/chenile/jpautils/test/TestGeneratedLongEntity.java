package org.chenile.jpautils.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaGeneratedLongEntity;
import org.chenile.jpautils.entity.ChenileGeneratedId;

@Entity
@Table(name = "test_generated_long_entity")
@ChenileGeneratedId(strategy = "longId")
public class TestGeneratedLongEntity extends BaseJpaGeneratedLongEntity {
}
