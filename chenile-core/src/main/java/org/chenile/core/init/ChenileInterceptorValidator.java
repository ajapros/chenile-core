package org.chenile.core.init;

import org.chenile.core.model.ChenileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * Resolves the deployable-wide interceptors named in <code>chenile.pre.processors</code>,
 * <code>chenile.post.processors</code> and <code>chenile.exception.handler</code> once all
 * singletons exist and before the application starts taking traffic. A misspelt or mistyped
 * name therefore fails startup with a clear error instead of failing (or being skipped) at
 * request time.
 * <p>Set <code>chenile.interceptors.validate-on-startup=false</code> to defer resolution to
 * the first request. Resolution is all-or-nothing either way.</p>
 */
public class ChenileInterceptorValidator implements SmartInitializingSingleton {
	private static final Logger logger = LoggerFactory.getLogger(ChenileInterceptorValidator.class);
	private final ChenileConfiguration chenileConfiguration;
	private final boolean validateOnStartup;

	public ChenileInterceptorValidator(ChenileConfiguration chenileConfiguration, boolean validateOnStartup) {
		this.chenileConfiguration = chenileConfiguration;
		this.validateOnStartup = validateOnStartup;
	}

	@Override
	public void afterSingletonsInstantiated() {
		if (!validateOnStartup) {
			logger.warn("chenile.interceptors.validate-on-startup=false: deployable-wide interceptors "
					+ "will be resolved on the first request");
			return;
		}
		chenileConfiguration.validateInterceptors();
	}
}
