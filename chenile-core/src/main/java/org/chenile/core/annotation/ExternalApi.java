package org.chenile.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ChenileAnnotation
public @interface ExternalApi {
    String system();
    String operation() default "";
    boolean enabled() default true;
}
