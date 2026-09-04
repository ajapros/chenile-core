package org.chenile.jpautils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chenile.core.context.ContextContainer;
import org.chenile.utils.entity.service.IDGenerator;
import org.chenile.utils.entity.service.TypedIdGenerationRequest;
import org.chenile.utils.entity.service.TypedIdGenerationStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.dao.InvalidDataAccessApiUsageException;
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

		@Bean("longId")
		TypedIdGenerationStrategy<Long> longId() {
			return new TypedIdGenerationStrategy<>() {
				@Override public Class<Long> idType() { return Long.class; }
				@Override public Long generate(TypedIdGenerationRequest request) { return 901L; }
			};
		}

		@Bean("integerId")
		TypedIdGenerationStrategy<Integer> integerId() {
			return new TypedIdGenerationStrategy<>() {
				@Override public Class<Integer> idType() { return Integer.class; }
				@Override public Integer generate(TypedIdGenerationRequest request) { return 42; }
			};
		}
	}

	@Resource
	private TestEntityRepository repository;
	@Resource
	private TestLongEntityRepository longRepository;
	@Resource
	private TestIntegerEntityRepository integerRepository;
	@Resource
	private TestGeneratedLongEntityRepository generatedLongRepository;
	@Resource
	private TestGeneratedIntegerEntityRepository generatedIntegerRepository;
	@Resource
	private TestUnconfiguredGeneratedLongEntityRepository unconfiguredGeneratedLongRepository;

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

	@Test
	void letsJpaGenerateLongAndIntegerPrimaryKeys() {
		TestLongEntity savedLong = longRepository.save(new TestLongEntity());
		TestIntegerEntity savedInteger = integerRepository.save(new TestIntegerEntity());

		assertTrue(savedLong.getId() > 0);
		assertTrue(savedInteger.getId() > 0);
	}

	@Test
	void usesNamedTypedGeneratorsForLongAndIntegerPrimaryKeys() {
		assertEquals(901L, generatedLongRepository.save(new TestGeneratedLongEntity()).getId());
		assertEquals(42, generatedIntegerRepository.save(new TestGeneratedIntegerEntity()).getId());
	}

	@Test
	void capturesChenileAuditFieldsForNumericEntities() {
		ContextContainer.CONTEXT_CONTAINER.setTenant("tenant-jpa");
		ContextContainer.CONTEXT_CONTAINER.put("x-chenile-auth-user", "jpa-user");
		ContextContainer.CONTEXT_CONTAINER.setTestMode("true");

		TestLongEntity saved = longRepository.save(new TestLongEntity());

		assertEquals("tenant-jpa", saved.tenant);
		assertEquals("jpa-user", saved.createdBy);
		assertTrue(saved.testEntity);
		assertNotNull(saved.getCreatedTime());
	}

	@Test
	void rejectsGeneratedNumericEntitiesWithoutAStrategyAnnotation() {
		InvalidDataAccessApiUsageException exception = assertThrows(InvalidDataAccessApiUsageException.class,
				() -> unconfiguredGeneratedLongRepository.save(new TestUnconfiguredGeneratedLongEntity()));
		assertTrue(exception.getCause() instanceof IllegalStateException);
	}
}
