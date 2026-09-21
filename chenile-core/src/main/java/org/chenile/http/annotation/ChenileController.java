/**
 *
 */
package org.chenile.http.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares a Chenile controller. A controller is a Chenile service declaration;
 * it is not necessarily an HTTP endpoint. When the same bean is also annotated
 * with Spring's {@code @RestController}, chenile-http supplies the HTTP binding.
 *
 * <p>The package name is retained for source compatibility. The annotation is
 * supplied by the chenile-core artifact so event-only and serverless
 * applications do not need a chenile-http dependency.</p>
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface ChenileController {
	String value();
	String serviceName() default "";
	String serviceModule() default "";
	String bluePrintName() default "";
	ChenileAdditionalAttribute[] additionalAttributes() default {};
	String healthCheckerName() default "";
	String mockName() default "";
	Class<?> interfaceClass() default Object.class;

	/**
	 * Whether this service definition is published to the remote Chenile Service
	 * Registry. It is always registered locally in {@code ChenileConfiguration}.
	 */
	boolean registerInServiceRegistry() default true;
}
