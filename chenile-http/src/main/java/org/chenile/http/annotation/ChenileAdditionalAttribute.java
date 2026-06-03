package org.chenile.http.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Retention(RUNTIME)
@Documented
public @interface ChenileAdditionalAttribute {
	String key();
	String value();
}
