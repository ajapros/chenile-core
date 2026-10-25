package org.chenile.core.model;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import org.chenile.core.interceptors.ChenileExceptionHandler;
import org.chenile.owiz.Command;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Deployable-wide interceptors must resolve all-or-nothing: a bad name must never leave a
 * partly built list cached (the first request failing and later ones silently skipping
 * the remaining interceptors).
 */
public class ChenileConfigurationInterceptorsTest {

	static class Noop extends BaseChenileInterceptor {}

	private GenericApplicationContext context(String... beans) {
		GenericApplicationContext ctx = new GenericApplicationContext();
		for (String b : beans) ctx.registerBean(b, Noop.class, Noop::new);
		ctx.registerBean("notAnInterceptor", String.class, () -> "x");
		ctx.registerBean("chenileExceptionHandler", ChenileExceptionHandler.class, ChenileExceptionHandler::new);
		ctx.refresh();
		return ctx;
	}

	private ChenileConfiguration config(GenericApplicationContext ctx, String pre, String post) {
		ChenileConfiguration c = new ChenileConfiguration("test", ctx);
		c.addPreProcessors(pre);
		c.addPostProcessors(post);
		c.setChenileExceptionHandlerName("chenileExceptionHandler");
		return c;
	}

	private static String subError(ConfigurationException e) {
		return e.getSubErrorNum();
	}

	@Test public void resolvesInOrderAndToleratesSpacesAndBlanks() {
		GenericApplicationContext ctx = context("a", "b");
		ChenileConfiguration c = config(ctx, " a , b ,", null);
		List<Command<ChenileExchange>> pre = c.getPreProcessorCommands();
		assertEquals(2, pre.size());
		assertSame(ctx.getBean("a"), pre.get(0));
		assertSame(ctx.getBean("b"), pre.get(1));
		assertSame(pre, c.getPreProcessorCommands());
		assertNull(c.getPostProcessorCommands());
	}

	@Test public void missingBeanFailsEveryTimeAndCachesNothing() {
		ChenileConfiguration c = config(context("a", "c"), "a,typo,c", "");
		for (int attempt = 0; attempt < 2; attempt++) {
			try {
				c.getPreProcessorCommands();
				fail("expected a configuration error on attempt " + attempt);
			} catch (ConfigurationException e) {
				assertEquals("526", subError(e));
			}
		}
	}

	@Test public void wrongTypeIsRejected() {
		ChenileConfiguration c = config(context(), null, "notAnInterceptor");
		try {
			c.getPostProcessorCommands();
			fail("expected a configuration error");
		} catch (ConfigurationException e) {
			assertEquals("527", subError(e));
		}
	}

	@Test public void validateInterceptorsChecksAllThree() {
		config(context("a"), "a", "a").validateInterceptors();

		ChenileConfiguration badHandler = config(context("a"), "a", null);
		badHandler.setChenileExceptionHandlerName("noSuchHandler");
		try {
			badHandler.validateInterceptors();
			fail("expected a configuration error");
		} catch (ConfigurationException e) {
			assertEquals("526", subError(e));
		}
	}
}
