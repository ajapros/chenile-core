package org.chenile.jpautils.bdd;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaGeneratedLongEntity;
import org.chenile.jpautils.entity.ChenileGeneratedId;

@Entity
@Table(name = "bdd_long_entity")
@ChenileGeneratedId(strategy = "bddLongId")
public class BddLongEntity extends BaseJpaGeneratedLongEntity {
}
