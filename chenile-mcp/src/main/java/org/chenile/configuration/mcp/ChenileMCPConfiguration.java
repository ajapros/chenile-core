package org.chenile.configuration.mcp;


import org.chenile.mcp.init.ChenileMCPInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ChenileMCPConfiguration {

    @Value("${chenile.mcp.enabled:true}") private boolean mcpEnabled;

    @Bean
    public ChenileMCPInitializer chenileMCPInitializer(){
        return new ChenileMCPInitializer(mcpEnabled);
    }
}
