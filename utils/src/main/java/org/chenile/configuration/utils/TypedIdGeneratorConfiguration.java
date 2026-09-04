package org.chenile.configuration.utils;

import jakarta.annotation.PostConstruct;
import org.chenile.utils.entity.service.TypedIdGenerationStrategy;
import org.chenile.utils.entity.service.TypedIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/** Registers the additive typed ID generator API without altering legacy String generators. */
@Configuration
public class TypedIdGeneratorConfiguration {
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void registerStrategies() {
        Map<String, TypedIdGenerationStrategy> strategies =
                applicationContext.getBeansOfType(TypedIdGenerationStrategy.class);
        for (Map.Entry<String, TypedIdGenerationStrategy> entry : strategies.entrySet()) {
            TypedIdGenerator.registerStrategy(entry.getKey(), entry.getValue());
        }
    }
}
