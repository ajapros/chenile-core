package org.chenile.mcp.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.chenile.mcp.init.ChenileMCPInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.ai.util.json.schema.JsonSchemaUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class ChenileToolCallback implements ToolCallback {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static Logger logger = LoggerFactory.getLogger(ChenileToolCallback.class);

    private final ToolDefinition toolDefinition;
    private final ToolMetadata toolMetadata;
    private final Method method;
    private final Object serviceReference;
    private final List<ChenileToolParameter> parameters;
    private final ToolCallResultConverter toolCallResultConverter;

    public ChenileToolCallback(ToolDefinition toolDefinition,
                               ToolMetadata toolMetadata,
                               Method method,
                               Object serviceReference,
                               List<ChenileToolParameter> parameters) {
        this.toolDefinition = toolDefinition;
        this.toolMetadata = toolMetadata;
        this.method = method;
        this.serviceReference = serviceReference;
        this.parameters = List.copyOf(parameters);
        this.toolCallResultConverter = new DefaultToolCallResultConverter();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return toolMetadata;
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        try {
            JsonNode inputNode = (toolInput == null || toolInput.isBlank())
                    ? OBJECT_MAPPER.createObjectNode()
                    : OBJECT_MAPPER.readTree(toolInput);
            Object[] args = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                ChenileToolParameter parameter = parameters.get(i);
                Object value = parameter.fixedValue();
                if (value == null) {
                    JsonNode valueNode = inputNode.get(parameter.name());
                    value = convert(valueNode, parameter.type());
                }
                args[i] = value;
            }
            Object result = method.invoke(serviceReference, args);
            return toolCallResultConverter.convert(result, method.getGenericReturnType());
        } catch (Exception e) {
            throw new RuntimeException("Error invoking MCP tool " + toolDefinition.name(), e);
        }
    }

    private Object convert(JsonNode node, TypeReference<?> targetType) throws Exception {
        if (node == null || node.isNull()) {
            return null;
        }
        if (targetType == null) {
            return OBJECT_MAPPER.treeToValue(node, Object.class);
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(targetType.getType());
        if (javaType.getRawClass() == Object.class) {
            return OBJECT_MAPPER.treeToValue(node, Object.class);
        }
        if (javaType.getRawClass() == String.class && node.isTextual()) {
            return node.asText();
        }
        return OBJECT_MAPPER.readerFor(javaType).readValue(node);
    }

    public static String buildInputSchema(List<ChenileToolParameter> parameters) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("type", "object");
        ObjectNode properties = root.putObject("properties");
        for (ChenileToolParameter parameter : parameters) {
            if (parameter.fixedValue() != null) {
                continue;
            }
            JsonNode schemaNode;
            try {
                Type parameterType = parameter.type() == null ? Object.class : parameter.type().getType();
                logger.info("Parameter type = {}",parameterType);
                schemaNode = OBJECT_MAPPER.readTree(JsonSchemaGenerator.generateForType(parameterType));
            } catch (Exception e) {
                throw new RuntimeException("Error generating schema for " + parameter.name(), e);
            }
            if (parameter.description() != null && !parameter.description().isBlank()
                    && schemaNode instanceof ObjectNode schemaObject) {
                schemaObject.put("description", parameter.description());
            }
            properties.set(parameter.name(), schemaNode);
        }
        removeRequiredFields(root);
        logger.info("Generating input Schema = {}",root.toPrettyString());
        return JsonSchemaUtils.ensureValidInputSchema(root.toString());
    }

    private static void removeRequiredFields(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof ObjectNode objectNode) {
            objectNode.remove("required");
            objectNode.fields().forEachRemaining(entry -> removeRequiredFields(entry.getValue()));
            return;
        }
        if (node.isArray()) {
            node.forEach(ChenileToolCallback::removeRequiredFields);
        }
    }

    public record ChenileToolParameter(String name, String description,TypeReference<?> type, Object fixedValue) {
    }
}
