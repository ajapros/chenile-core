package org.chenile.configuration.multids;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.chenile.core.context.ContextContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MultiTenantDataSourceProperties.class)
public class MultiTenantDataSourceConfiguration {

    @Bean("multiTenantTargetDataSources")
    Map<String, DataSource> multiTenantTargetDataSources(
            @Autowired MultiTenantDataSourceProperties properties) {
        Map<String, DataSource> targetDataSources = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : properties.getDatasources().entrySet()) {
            Properties hikariProps = new Properties();
            for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
                if ("type".equals(prop.getKey())) {
                    continue;
                }
                hikariProps.setProperty(prop.getKey(), prop.getValue());
            }
            HikariConfig hikariConfig = new HikariConfig(hikariProps);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            targetDataSources.put(entry.getKey(), dataSource);
        }
        return targetDataSources;
    }

    @Bean
    @Primary
    DataSource dataSource(@Autowired @Qualifier("multiTenantTargetDataSources") Map<String, DataSource> targetDataSources,
                          @Autowired MultiTenantDataSourceProperties properties,
                          @Autowired ContextContainer contextContainer) {
        if (targetDataSources.isEmpty()) {
            throw new IllegalStateException("chenile.multids.datasources is empty or not configured");
        }
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String tenantId = contextContainer.getTenant();
                if (tenantId == null) {
                    return null;
                }
                tenantId = tenantId.trim();
                return tenantId.isEmpty() ? null : tenantId;
            }
        };
        Map<Object, Object> target = new LinkedHashMap<>(targetDataSources);
        routingDataSource.setTargetDataSources(target);
        String defaultTenantId = properties.getDefaultTenantId();
        DataSource defaultDataSource = defaultTenantId == null ? null : targetDataSources.get(defaultTenantId);
        if (defaultDataSource == null && !targetDataSources.isEmpty()) {
            defaultDataSource = targetDataSources.values().iterator().next();
        }
        assert defaultDataSource != null;
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        return routingDataSource;
    }
}
