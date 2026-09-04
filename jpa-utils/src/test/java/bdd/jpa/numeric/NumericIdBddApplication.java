package bdd.jpa.numeric;

import org.chenile.configuration.utils.TypedIdGeneratorConfiguration;
import org.chenile.jpautils.bdd.BddLongEntity;
import org.chenile.jpautils.bdd.BddLongEntityRepository;
import org.chenile.jpautils.test.TestEntity;
import org.chenile.jpautils.test.TestEntityRepository;
import org.chenile.utils.entity.service.TypedIdGenerationRequest;
import org.chenile.utils.entity.service.TypedIdGenerationStrategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Isolated Spring configuration for the numeric ID Cucumber suite. */
@SpringBootApplication(scanBasePackages = "bdd.jpa.numeric.support")
@EnableJpaRepositories(basePackageClasses = {TestEntityRepository.class, BddLongEntityRepository.class})
@EntityScan(basePackageClasses = {TestEntity.class, BddLongEntity.class})
@Import(TypedIdGeneratorConfiguration.class)
public class NumericIdBddApplication {
    @Bean("bddLongId")
    TypedIdGenerationStrategy<Long> bddLongId() {
        return new TypedIdGenerationStrategy<>() {
            @Override public Class<Long> idType() { return Long.class; }
            @Override public Long generate(TypedIdGenerationRequest request) { return 7001L; }
        };
    }
}
