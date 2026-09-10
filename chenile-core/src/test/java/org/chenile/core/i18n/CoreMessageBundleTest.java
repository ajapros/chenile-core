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
	void cannotInvokeTargetRendersTypeOperationAndMessageInEveryBundle() {
		MultipleMessageSource messageSource = new MultipleMessageSource();
		messageSource.setBasename("classpath*:messages");
		messageSource.setFallbackToSystemLocale(false);
		Object[] params = { "target method failed", "leadService.fetch", "IllegalStateException" };

		for (Locale locale : new Locale[] { Locale.US, Locale.ENGLISH, Locale.ROOT }) {
			assertEquals("Unexpected IllegalStateException while invoking leadService.fetch: target method failed",
					messageSource.getMessage("E509", params, locale),
					"E509 resolved for locale '" + locale + "'");
		}
	}
}
