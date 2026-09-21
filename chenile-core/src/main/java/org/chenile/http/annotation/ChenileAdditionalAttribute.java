package org.chenile.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Additional metadata attached to a {@link ChenileController} declaration. */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface ChenileAdditionalAttribute {
	String key();
	String value();
}
