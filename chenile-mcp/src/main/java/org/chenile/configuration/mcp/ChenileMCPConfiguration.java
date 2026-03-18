package org.chenile.configuration.mcp;


import org.chenile.core.model.ChenileConfiguration;
import org.chenile.http.init.AnnotationChenileServiceInitializer;
import org.chenile.mcp.init.ChenileMCPInitializer;
import org.chenile.mcp.model.ChenilePolymorphProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import java.util.Map;


@Configuration
public class ChenileMCPConfiguration {

    @Value("${chenile.mcp.enabled:true}") private boolean mcpEnabled;

    @Bean
    @DependsOn("annotationChenileServiceInitializer")
    public ChenileMCPInitializer chenileMCPInitializer(ChenileConfiguration chenileConfiguration,
                                                       Map<String, ChenilePolymorphProvider> polymorphProviderMap){
        return new ChenileMCPInitializer(mcpEnabled,chenileConfiguration,polymorphProviderMap);
    }
}
