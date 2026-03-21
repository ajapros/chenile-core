package org.chenile.mcp.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        Assertions.assertTrue(callbacksByName.get("simpleTool").getToolDefinition().inputSchema()
                .contains("\"description\":\"Simple payload body\""));
        Assertions.assertTrue(callbacksByName.get("fooTool_e1").getToolDefinition().inputSchema()
                .contains("\"description\":\"Payload for event e1\""));
        Assertions.assertTrue(callbacksByName.get("fooTool_e2").getToolDefinition().inputSchema()
                .contains("\"description\":\"Payload for event e2\""));
        callbacksByName.values().forEach(callback ->
                Assertions.assertFalse(containsRequired(readSchema(callback)),
                        "Schema should not contain required fields for tool " + callback.getToolDefinition().name()));

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

    private JsonNode readSchema(ToolCallback callback) {
        try {
            return OBJECT_MAPPER.readTree(callback.getToolDefinition().inputSchema());
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse tool schema", e);
        }
    }

    private boolean containsRequired(JsonNode node) {
        if (node == null) {
            return false;
        }
        if (node.isObject()) {
            if (node.has("required")) {
                return true;
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                if (containsRequired(fields.next().getValue())) {
                    return true;
                }
            }
            return false;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (containsRequired(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
