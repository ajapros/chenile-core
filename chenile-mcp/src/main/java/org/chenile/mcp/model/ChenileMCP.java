package org.chenile.mcp.model;

import org.chenile.core.annotation.ChenileAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a Chenile controller as MCP-enabled and carries descriptive metadata.
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ChenileAnnotation
public @interface ChenileMCP {
    String name() default "";
    String description() default "";
}
