package org.chenile.core.interceptors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Locale;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.i18n.MultipleMessageSource;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.junit.jupiter.api.Test;

/**
 * Checks how ServiceInvoker reports an exception that is not an ErrorNumException.
 */
class ServiceInvokerTest {

	@Test
	void unmodeledExceptionIsWrappedWithItsMessageOperationAndType() throws NoSuchMethodException {
		ChenileExchange exchange = exchangeFor("fail");

		new ServiceInvoker().execute(exchange);

		ErrorNumException exception = exchange.getException();
		assertEquals("509", exception.getSubErrorNum());
		assertArrayEquals(new Object[] { "target method failed", "leadService.fail", "IllegalStateException" },
				exception.getParams());
	}

	@Test
	void wrappedExceptionRendersThroughTheE509Template() throws NoSuchMethodException {
		ChenileExchange exchange = exchangeFor("fail");
		new ServiceInvoker().execute(exchange);
		MultipleMessageSource messageSource = new MultipleMessageSource();
		messageSource.setBasename("classpath*:messages");

		String description = messageSource.getMessage("E" + exchange.getException().getSubErrorNum(),
				exchange.getException().getParams(), Locale.US);

		assertEquals("Unexpected IllegalStateException while invoking leadService.fail: target method failed",
				description);
	}

	private static ChenileExchange exchangeFor(String operationName) throws NoSuchMethodException {
		ChenileServiceDefinition serviceDefinition = new ChenileServiceDefinition();
		serviceDefinition.setName("leadService");
		OperationDefinition operationDefinition = new OperationDefinition();
		operationDefinition.setName(operationName);
		ChenileExchange exchange = new ChenileExchange();
		exchange.setServiceDefinition(serviceDefinition);
		exchange.setOperationDefinition(operationDefinition);
		exchange.setServiceReference(new FailingService());
		exchange.setMethod(FailingService.class.getMethod(operationName));
		exchange.setApiInvocation(new ArrayList<>());
		return exchange;
	}

	public static class FailingService {
		public void fail() {
			throw new IllegalStateException("target method failed");
		}
	}
}
