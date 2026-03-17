package org.chenile.mcp.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = {"org.chenile.configuration", "org.chenile.mcp.configuration", "org.chenile.mcp.test"})
@PropertySource("classpath:org/chenile/mcp/test/TestChenileMcp.properties")
public class McpTestApplication {
}
