package org.chenile.configuration.utils;

import java.util.Map;

import org.chenile.utils.entity.service.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class IdGeneratorConfiguration {

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${chenile.id.generator.defaultStrategy:default}")
	private String defaultStrategyName;

	@PostConstruct
	public void registerStrategies() {
		Map<String, IDGenerator.IdGenerationStrategy> strategies =
				applicationContext.getBeansOfType(IDGenerator.IdGenerationStrategy.class);
		for (Map.Entry<String, IDGenerator.IdGenerationStrategy> entry : strategies.entrySet()) {
			IDGenerator.registerStrategy(entry.getKey(), entry.getValue());
		}
		IDGenerator.setDefaultStrategyName(defaultStrategyName);
	}
}
