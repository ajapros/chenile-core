package org.chenile.utils.entity.service;

import org.chenile.SpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {SpringConfig.class, TypedIdGeneratorTest.StrategyTestConfig.class})
class TypedIdGeneratorTest {
    @TestConfiguration
    static class StrategyTestConfig {
        @Bean("longTypedId")
        TypedIdGenerationStrategy<Long> longTypedId() {
            return new TypedIdGenerationStrategy<>() {
                @Override public Class<Long> idType() { return Long.class; }
                @Override public Long generate(TypedIdGenerationRequest request) { return 73L; }
            };
        }

        @Bean("stringTypedId")
        TypedIdGenerationStrategy<String> stringTypedId() {
            return new TypedIdGenerationStrategy<>() {
                @Override public Class<String> idType() { return String.class; }
                @Override public String generate(TypedIdGenerationRequest request) { return "73"; }
            };
        }

        @SuppressWarnings("rawtypes")
        @Bean("invalidRuntimeTypedId")
        TypedIdGenerationStrategy invalidRuntimeTypedId() {
            return new TypedIdGenerationStrategy() {
                @Override public Class idType() { return Long.class; }
                @Override public Object generate(TypedIdGenerationRequest request) { return "not-a-long"; }
            };
        }
    }

    @Test
    void generatesIdsUsingTheDeclaredType() {
        assertEquals(73L, TypedIdGenerator.generateId("longTypedId", Long.class, "Order", Object.class));
    }

    @Test
    void rejectsMissingOrIncompatibleStrategiesClearly() {
        assertThrows(IllegalStateException.class,
                () -> TypedIdGenerator.generateId("missingTypedId", Long.class, "Order", Object.class));
        assertThrows(IllegalStateException.class,
                () -> TypedIdGenerator.generateId("stringTypedId", Long.class, "Order", Object.class));
        assertThrows(IllegalStateException.class,
                () -> TypedIdGenerator.generateId("invalidRuntimeTypedId", Long.class, "Order", Object.class));
    }
}
