package org.chenile.configuration.multids;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "chenile.multids")
public class MultiTenantDataSourceProperties {
    private Map<String, Map<String, String>> datasources = new LinkedHashMap<>();
    private String defaultTenantId;

    public Map<String, Map<String, String>> getDatasources() {
        return datasources;
    }

    public void setDatasources(Map<String, Map<String, String>> datasources) {
        this.datasources = datasources;
    }

    public String getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }
}
