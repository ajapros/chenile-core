package org.chenile.multids;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class TestDataInitializerConfig {

    @Bean
    ApplicationRunner multiTenantDataInitializer(
            @Qualifier("multiTenantTargetDataSources") Map<String, DataSource> targetDataSources) {
        return args -> {
            Resource schema = new ClassPathResource("db/schema.sql");
            for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
                String dataFileName = "db/data-" + entry.getKey() + ".sql";
                Resource data = new ClassPathResource(dataFileName);
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator(schema, data);
                DatabasePopulatorUtils.execute(populator, entry.getValue());
            }
        };
    }
}
