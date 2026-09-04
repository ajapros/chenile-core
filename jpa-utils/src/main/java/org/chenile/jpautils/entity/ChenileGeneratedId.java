package org.chenile.jpautils.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Selects the named typed generator used by a Chenile-generated numeric JPA entity. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChenileGeneratedId {
    String strategy();
}
