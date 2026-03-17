package org.chenile.mcp.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest(classes = McpTestApplication.class)
@ActiveProfiles("unittest")
public class TestChenileMcp {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    public void testChenileMcpAndPolymorphRegistration() {
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        Assertions.assertEquals(3, callbacks.length);

        Map<String, ToolCallback> callbacksByName = Arrays.stream(callbacks)
                .collect(Collectors.toMap(callback -> callback.getToolDefinition().name(), Function.identity()));

        Assertions.assertTrue(callbacksByName.containsKey("simpleTool"));
        Assertions.assertTrue(callbacksByName.containsKey("fooTool_e1"));
        Assertions.assertTrue(callbacksByName.containsKey("fooTool_e2"));

        String simpleResult = callbacksByName.get("simpleTool")
                .call("{\"payload\":{\"value\":\"alpha\"}}");
        Assertions.assertEquals("\"simple:alpha\"", simpleResult);

        String e1Result = callbacksByName.get("fooTool_e1")
                .call("{\"id\":\"42\",\"eventPayload\":{\"value\":\"one\"}}");
        Assertions.assertEquals("\"e1:E1:one\"", e1Result);

        String e2Result = callbacksByName.get("fooTool_e2")
                .call("{\"id\":\"43\",\"eventPayload\":{\"value\":\"two\"}}");
        Assertions.assertEquals("\"e2:E2:two\"", e2Result);
    }
}
