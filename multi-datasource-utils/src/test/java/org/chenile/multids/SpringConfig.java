package org.chenile.multids;

import org.chenile.configuration.multids.MultiTenantDataSourceConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@SpringBootApplication(scanBasePackages = {"org.chenile"})
@PropertySource("classpath:org/chenile/multids/TestService.properties")
@ActiveProfiles("unittest")
public class SpringConfig extends SpringBootServletInitializer {
}
