package org.chenile.core.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Checks that chenile-core's own message bundles substitute their parameters.
 * Each locale below resolves from a different bundle file, so every copy of a template is covered.
 */
class CoreMessageBundleTest {

	@Test
	void cannotInvokeTargetRendersTheWrappedExceptionMessageInEveryBundle() {
		MultipleMessageSource messageSource = new MultipleMessageSource();
		messageSource.setBasename("classpath*:messages");
		messageSource.setFallbackToSystemLocale(false);

		for (Locale locale : new Locale[] { Locale.US, Locale.ENGLISH, Locale.ROOT }) {
			assertEquals("CANNOT_INVOKE_TARGET: target method failed",
					messageSource.getMessage("E509", new Object[] { "target method failed" }, locale),
					"E509 resolved for locale '" + locale + "'");
		}
	}
}
