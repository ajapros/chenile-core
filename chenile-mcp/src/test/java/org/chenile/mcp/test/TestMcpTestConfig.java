package org.chenile.mcp.test;

import tools.jackson.core.type.TypeReference;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.mcp.model.ChenilePolymorphProvider;
import org.chenile.mcp.model.ChenilePolymorphVariant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class TestMcpTestConfig {

    @Bean
    public TestMcpService mcpTestService() {
        return new TestMcpService();
    }

    /**
     * For event ID e1 we will need the payload to be {@link E1Payload}
     * For event ID e2 we will need the payload to be {@link E2Payload}
     * This is captured in the combination of fixed values and the types.
     * @return
     */
    @Bean
    public ChenilePolymorphProvider testPolymorphProvider() {
        return new ChenilePolymorphProvider() {
            @Override
            public List<ChenilePolymorphVariant> getVariants(ChenileServiceDefinition serviceDefinition,
                                                             OperationDefinition operationDefinition) {
                return List.of(
                        new ChenilePolymorphVariant(
                                "e1",
                                "Event e1 variant",
                                Map.of("eventPayload", new TypeReference<E1Payload>() {}),
                                Map.of("eventPayload", "Payload for event e1"),
                                Map.of("eventId", "e1")
                        ),
                        new ChenilePolymorphVariant(
                                "e2",
                                "Event e2 variant",
                                Map.of("eventPayload", new TypeReference<E2Payload>() {}),
                                Map.of("eventPayload", "Payload for event e2"),
                                Map.of("eventId", "e2")
                        )
                );
            }
        };
    }
}
