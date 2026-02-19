package org.chenile.jpautils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chenile.core.context.ContextContainer;
import org.chenile.utils.entity.service.IDGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {IdGenerationJpaTest.TestApp.class, IdGenerationJpaTest.StrategyConfig.class})
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:jpautils-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false"
})
public class IdGenerationJpaTest {

	@SpringBootApplication(scanBasePackages = {"org.chenile"})
	@EnableJpaRepositories(basePackages = "org.chenile.jpautils.test")
	@EntityScan(basePackages = "org.chenile.jpautils.test")
	static class TestApp {}

	@TestConfiguration
	static class StrategyConfig {
		@Bean("customStrategy")
		IDGenerator.IdGenerationStrategy customStrategy() {
			return (prefix, contextContainer) -> "CUSTOM-" + prefix;
		}
	}

	@Resource
	private TestEntityRepository repository;

	@AfterEach
	void cleanup() {
		ContextContainer.CONTEXT_CONTAINER.clear();
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, null);
	}

	@Test
	void generatesIdWithCustomStrategy() {
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, "customStrategy");
		TestEntity entity = new TestEntity();
		TestEntity saved = repository.save(entity);
		assertEquals("CUSTOM-TestEntity", saved.getId());
	}

	@Test
	void fallsBackToDefaultWhenStrategyMissing() {
		ContextContainer.CONTEXT_CONTAINER.setRequestId("REQX");
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, "missingStrategy");
		TestEntity entity = new TestEntity();
		TestEntity saved = repository.save(entity);
		assertTrue(saved.getId().startsWith("TestEntity-REQX-"));
	}

	@Test
	void fallsBackToDefaultWhenStrategyMissingAndMissingRequest() {
		ContextContainer.putExtension(IDGenerator.STRATEGY_KEY, "missingStrategy");
		TestEntity entity = new TestEntity();
		TestEntity saved = repository.save(entity);
		System.out.println(saved.getId());
		assertTrue(saved.getId().startsWith("TestEntity"));
		assertTrue(saved.getId().endsWith("0001"));
		assertTrue(saved.getId().length()>40);
	}
}
