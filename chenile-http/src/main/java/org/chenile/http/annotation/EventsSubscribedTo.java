package org.chenile.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation specifies that this particular method subscribes to an event.
 * This can be a local or global event. Local events are executed in the same JVM and are
 * directly invoked using {@link org.chenile.core.event.EventProcessor}
 * <p>Remote events are triggered by Chenile Pub Sub group of modules. This requires a dependency on
 * chenile-kafka or other modules. Global events create local events at the transport end point</p>
 * <p>For example, an event such as a message arriving at a queue or topic can translate into
 * Chenile event. </p>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface EventsSubscribedTo {
	public String[] value();
}
