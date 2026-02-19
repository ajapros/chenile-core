package org.chenile.utils.entity.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.chenile.SpringConfig;
import org.chenile.core.context.ContextContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(classes = {SpringConfig.class, IDGeneratorStrategyTest.StrategyTestConfig.class})
public class IDGeneratorStrategyTest {

	@TestConfiguration
	static class StrategyTestConfig {
		@Bean("customStrategy")
		IDGenerator.IdGenerationStrategy customStrategy() {
			return (prefix, contextContainer) -> "CUSTOM-" + prefix;
		}
	}

	@AfterEach
	void cleanup() {
		ContextContainer.CONTEXT_CONTAINER.clear();
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, null);
	}

	@Test
	void usesCustomStrategyWhenPresent() {
		ContextContainer.CONTEXT_CONTAINER.setRequestId("REQ1");
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, "customStrategy");
		String id = IDGenerator.generateID("Order");
		assertEquals("CUSTOM-Order", id);
	}

	@Test
	void fallsBackToDefaultWhenStrategyMissing() {
		ContextContainer.CONTEXT_CONTAINER.setRequestId("REQX");
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, "missingStrategy");
		String id = IDGenerator.generateID("Order");
		assertTrue(id.startsWith("Order-REQX-"));
	}
}
