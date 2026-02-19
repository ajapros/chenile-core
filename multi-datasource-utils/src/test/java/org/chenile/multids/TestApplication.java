package org.chenile.multids;

import org.chenile.configuration.multids.MultiTenantDataSourceConfiguration;
import org.chenile.core.context.ContextContainer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MultiTenantDataSourceConfiguration.class)
public class TestApplication {

    @Bean
    ContextContainer contextContainer() {
        return ContextContainer.CONTEXT_CONTAINER;
    }
}
