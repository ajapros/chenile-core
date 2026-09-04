package org.chenile.jpautils.test;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaGeneratedLongEntity;

@Entity
@Table(name = "test_unconfigured_generated_long_entity")
public class TestUnconfiguredGeneratedLongEntity extends BaseJpaGeneratedLongEntity {
}
