package org.chenile.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Declares operation-specific Chenile interceptors. */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface InterceptedBy {
	String[] value() default {};
}
