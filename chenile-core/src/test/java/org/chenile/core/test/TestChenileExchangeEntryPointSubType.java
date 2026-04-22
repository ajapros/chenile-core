package org.chenile.core.test;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.EntryPointSubType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestChenileExchangeEntryPointSubType {

	@Test
	public void testDefaultEntryPointSubTypeIsNone() {
		ChenileExchange exchange = new ChenileExchange();
		assertEquals(EntryPointSubType.NONE, exchange.getEntryPointSubType());
		assertFalse(exchange.isProxyInvocation());
	}

	@Test
	public void testLocalInvocationSetsLocalProxySubType() {
		ChenileExchange exchange = new ChenileExchange();
		exchange.setLocalInvocation(true);
		assertEquals(EntryPointSubType.LOCAL_PROXY, exchange.getEntryPointSubType());
		assertTrue(exchange.isProxyInvocation());
	}

	@Test
	public void testExplicitRemoteProxySubTypeIsRecognized() {
		ChenileExchange exchange = new ChenileExchange();
		exchange.setEntryPointSubType(EntryPointSubType.REMOTE_PROXY);
		assertEquals(EntryPointSubType.REMOTE_PROXY, exchange.getEntryPointSubType());
		assertTrue(exchange.isProxyInvocation());
	}
}
