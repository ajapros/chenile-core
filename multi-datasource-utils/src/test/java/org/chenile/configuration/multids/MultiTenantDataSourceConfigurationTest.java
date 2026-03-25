package org.chenile.configuration.multids;

import org.chenile.core.context.ContextContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiTenantDataSourceConfigurationTest {

    private static final String DEFAULT_TENANT_YAML = "application-default-tenant.yml";
    private static final String NO_DEFAULT_TENANT_YAML = "application-no-default-tenant.yml";

    private final MultiTenantDataSourceConfiguration configuration = new MultiTenantDataSourceConfiguration();

    @Test
    void shouldFailWhenDefaultTenantIsNotConfiguredAndTenantIsMissing() throws Exception {
        MultiTenantDataSourceProperties properties = propertiesFrom(NO_DEFAULT_TENANT_YAML);
        ContextContainer contextContainer = mock(ContextContainer.class);
        when(contextContainer.getTenant()).thenReturn(null);

        DataSource dataSource = configuration.dataSource(targetDataSources(properties), properties, contextContainer);
        ((AbstractRoutingDataSource) dataSource).afterPropertiesSet();

        assertThatThrownBy(dataSource::getConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot determine target DataSource for lookup key [null]");
    }

    @Test
    void shouldFailWhenTenantLookupDoesNotMatchConfiguredDatasource() throws Exception {
        MultiTenantDataSourceProperties properties = propertiesFrom(DEFAULT_TENANT_YAML);
        ContextContainer contextContainer = mock(ContextContainer.class);
        when(contextContainer.getTenant()).thenReturn("missing");

        DataSource dataSource = configuration.dataSource(targetDataSources(properties), properties, contextContainer);
        ((AbstractRoutingDataSource) dataSource).afterPropertiesSet();

        assertThatThrownBy(dataSource::getConnection)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot determine target DataSource for lookup key [missing]");
    }

    @Test
    void shouldUseDefaultDatasourceWhenDefaultTenantIsConfiguredAndTenantIsMissing() throws Exception {
        MultiTenantDataSourceProperties properties = propertiesFrom(DEFAULT_TENANT_YAML);
        ContextContainer contextContainer = mock(ContextContainer.class);
        when(contextContainer.getTenant()).thenReturn(null);

        DataSource dataSource = configuration.dataSource(targetDataSources(properties), properties, contextContainer);
        ((AbstractRoutingDataSource) dataSource).afterPropertiesSet();

        assertThatNoException().isThrownBy(dataSource::getConnection);
    }

    @Test
    void shouldFailWhenConfiguredDefaultTenantDoesNotExist() {
        MultiTenantDataSourceProperties properties = propertiesFrom(DEFAULT_TENANT_YAML);
        properties.setDefaultTenantId("missing");

        assertThatThrownBy(() -> configuration.dataSource(targetDataSources(properties), properties, mock(ContextContainer.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chenile.multids.defaultTenantId 'missing' is not present in chenile.multids.datasources");
    }

    private Map<String, DataSource> targetDataSources(MultiTenantDataSourceProperties properties) {
        return configuration.multiTenantTargetDataSources(properties);
    }

    private MultiTenantDataSourceProperties propertiesFrom(String resourcePath) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(resourcePath));
        Properties yamlProperties = factory.getObject();
        Map<String, Object> source = new LinkedHashMap<>();
        if (yamlProperties != null) {
            for (String name : yamlProperties.stringPropertyNames()) {
                source.put(name, yamlProperties.getProperty(name));
            }
        }
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(resourcePath, source));
        return Binder.get(environment)
                .bind("chenile.multids", Bindable.of(MultiTenantDataSourceProperties.class))
                .orElseThrow(() -> new IllegalStateException("Unable to bind chenile.multids from " + resourcePath));
    }
}
