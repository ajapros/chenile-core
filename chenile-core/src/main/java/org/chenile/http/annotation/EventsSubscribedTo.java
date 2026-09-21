package org.chenile.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Declares events consumed by an operation. */
@Retention(RUNTIME)
@Target(METHOD)
public @interface EventsSubscribedTo {
	String[] value();
}
