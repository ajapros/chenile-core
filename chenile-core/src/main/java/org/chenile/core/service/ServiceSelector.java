package org.chenile.core.service;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.model.ChenileServiceDefinition;

/**
 * Selects an implementation bean for a Chenile service invocation.
 *
 * <p>The returned value is a Spring bean name. Return {@code null} to use the
 * service definition's configured implementation.</p>
 */
@FunctionalInterface
public interface ServiceSelector {
	String selectServiceReferenceName(ChenileExchange exchange, ChenileServiceDefinition serviceDefinition);
}
