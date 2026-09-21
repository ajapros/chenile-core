package org.chenile.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method of a non-HTTP {@code ChenileController} as a Chenile operation.
 * HTTP controllers continue to derive their operations from Spring mapping
 * annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ChenileOperation {
	/** Logical operation name. Defaults to the Java method name. */
	String value() default "";
}
