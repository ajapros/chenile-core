package org.chenile.mcp.model;

import org.chenile.core.annotation.ChenileAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks an MCP-enabled operation as polymorphic. The referenced bean returns the concrete
 * parameter combinations that should be exposed as separate MCP tools.
 */
@Retention(RUNTIME)
@Target(METHOD)
@ChenileAnnotation
public @interface ChenilePolymorph {
    String value();
}
