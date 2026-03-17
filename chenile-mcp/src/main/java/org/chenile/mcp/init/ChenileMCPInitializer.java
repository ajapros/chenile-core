package org.chenile.mcp.init;

import org.springframework.context.annotation.DependsOn;
import tools.jackson.core.type.TypeReference;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.core.util.MethodUtils;
import org.chenile.mcp.model.ChenileMCP;
import org.chenile.mcp.model.ChenilePolymorph;
import org.chenile.mcp.model.ChenilePolymorphProvider;
import org.chenile.mcp.model.ChenilePolymorphVariant;
import org.chenile.mcp.model.ChenileToolCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Detects the {@link ChenileMCP} annotated services and operations in the Chenile configuration,
 * exposes them as MCP tools,
 */
@DependsOn("annotationChenileServiceInitializer")
public class ChenileMCPInitializer implements ToolCallbackProvider {
    Logger logger = LoggerFactory.getLogger(ChenileMCPInitializer.class);
    @Autowired
    ChenileConfiguration chenileConfiguration;
    @Autowired
    ApplicationContext applicationContext;
    private final List<ToolCallback> toolCallbacks = new ArrayList<>();
    final boolean chenileMcpEnabled;

    public ChenileMCPInitializer(boolean enabled){
        this.chenileMcpEnabled = enabled;

    }
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        if (!chenileMcpEnabled) {
            logger.info("Chenile MCP is disabled. Skipping MCP tool registration.");
            return;
        }
        logger.info("Starting the Chenile MCP ecosystem");
        toolCallbacks.clear();
        for (ChenileServiceDefinition serviceDefinition : chenileConfiguration.getServices().values()) {
            ChenileMCP serviceMcp = serviceDefinition.getExtensionAsAnnotation(ChenileMCP.class);
            for (OperationDefinition operationDefinition : serviceDefinition.getOperations()) {
                ChenileMCP operationMcp = operationDefinition.getExtensionAsAnnotation(ChenileMCP.class);
                if (serviceMcp == null && operationMcp == null) {
                    continue;
                }
                ChenileMCP effectiveMcp = (operationMcp != null)? operationMcp: serviceMcp;
                ChenilePolymorph polymorph = operationDefinition.getExtensionAsAnnotation(ChenilePolymorph.class);
                if (polymorph == null) {
                    registerAsMCP(serviceDefinition, operationDefinition, effectiveMcp, null);
                    continue;
                }
                ChenilePolymorphProvider provider =
                        applicationContext.getBean(polymorph.value(), ChenilePolymorphProvider.class);
                List<ChenilePolymorphVariant> variants = provider.getVariants(serviceDefinition, operationDefinition);
                if (variants == null || variants.isEmpty()) {
                    logger.warn("No polymorph variants returned for {}.{} from {}",
                            serviceDefinition.getId(), operationDefinition.getName(), polymorph.value());
                    continue;
                }
                for (ChenilePolymorphVariant variant : variants) {
                    registerAsMCP(serviceDefinition, operationDefinition, effectiveMcp, variant);
                }
            }
        }
    }

    private void registerAsMCP(ChenileServiceDefinition serviceDefinition,
                                     OperationDefinition operationDefinition,
                                     ChenileMCP chenileMCP,
                                     ChenilePolymorphVariant polymorphVariant) {
        Object serviceReference = serviceDefinition.getServiceReference();
        if (serviceReference == null) {
            logger.warn("Skipping MCP tool {}.{} because the service reference is null",
                    serviceDefinition.getId(), operationDefinition.getName());
            return;
        }
        Method method = resolveMethod(operationDefinition, serviceReference);
        if (method == null) {
            logger.warn("Skipping MCP tool {}.{} because no invokable method was resolved",
                    serviceDefinition.getId(), operationDefinition.getName());
            return;
        }
        List<ChenileToolCallback.ChenileToolParameter> toolParameters =
                buildToolParameters(operationDefinition, polymorphVariant);
        ToolDefinition toolDefinition = ToolDefinition.builder()
                .name(computeToolName(serviceDefinition, operationDefinition, chenileMCP, polymorphVariant))
                .description(computeToolDescription(serviceDefinition, operationDefinition, chenileMCP,
                        polymorphVariant))
                .inputSchema(ChenileToolCallback.buildInputSchema(toolParameters))
                .build();
        ToolMetadata toolMetadata = ToolMetadata.builder().returnDirect(false).build();
        toolCallbacks.add(new ChenileToolCallback(toolDefinition, toolMetadata, method, serviceReference, toolParameters));
        logger.info("Registered MCP tool {} for Chenile operation {}.{}",
                toolDefinition.name(), serviceDefinition.getId(), operationDefinition.getName());
    }

    private Method resolveMethod(OperationDefinition operationDefinition,
                                 Object serviceReference) {
        Method method = operationDefinition.getMethod();
        if (method != null && method.getDeclaringClass().isAssignableFrom(serviceReference.getClass())) {
            return method;
        }
        return MethodUtils.computeMethod(serviceReference.getClass(), operationDefinition);
    }

    private String computeToolName(ChenileServiceDefinition serviceDefinition,
                                   OperationDefinition operationDefinition,
                                   ChenileMCP chenileMCP,
                                   ChenilePolymorphVariant polymorphVariant) {
        if (chenileMCP != null && !chenileMCP.name().isBlank()) {
            return appendVariantSuffix(chenileMCP.name(), polymorphVariant);
        }
        return appendVariantSuffix(serviceDefinition.getId() + "_" + operationDefinition.getName(), polymorphVariant);
    }

    private String computeToolDescription(ChenileServiceDefinition serviceDefinition,
                                          OperationDefinition operationDefinition,
                                          ChenileMCP chenileMCP,
                                          ChenilePolymorphVariant polymorphVariant) {
        if (chenileMCP != null && !chenileMCP.description().isBlank()) {
            return augmentDescription(chenileMCP.description(), operationDefinition, polymorphVariant);
        }
        if (operationDefinition.getDescription() != null && !operationDefinition.getDescription().isBlank()) {
            return augmentDescription(operationDefinition.getDescription(), operationDefinition, polymorphVariant);
        }
        if (polymorphVariant != null && polymorphVariant.description() != null &&
                !polymorphVariant.description().isBlank()) {
            return augmentDescription(polymorphVariant.description(), operationDefinition, polymorphVariant);
        }
        return augmentDescription("Chenile service " + serviceDefinition.getId() + " operation " +
                operationDefinition.getName(), operationDefinition, polymorphVariant);
    }

    private String augmentDescription(String description,
                                      OperationDefinition operationDefinition,
                                      ChenilePolymorphVariant polymorphVariant) {
        StringBuilder sb = new StringBuilder(description);
        String parameterSummary = buildParameterSummary(operationDefinition, polymorphVariant);
        if (!parameterSummary.isEmpty()) {
            sb.append(parameterSummary);
        }
        String fixedParameterSummary = buildFixedParameterSummary(polymorphVariant);
        if (!fixedParameterSummary.isEmpty()) {
            sb.append(fixedParameterSummary);
        }
        return sb.toString();
    }

    private String buildParameterSummary(OperationDefinition operationDefinition,
                                         ChenilePolymorphVariant polymorphVariant) {
        if (operationDefinition.getParams() == null || operationDefinition.getParams().isEmpty()) {
            return "";
        }
        Map<String, Object> fixedValues = polymorphVariant == null ? Map.of() : polymorphVariant.fixedParameterValues();
        Map<String, TypeReference<?>> parameterTypes = polymorphVariant == null ? Map.of() : polymorphVariant.parameterTypes();
        StringJoiner joiner = new StringJoiner(", ", " Parameters: ", "");
        for (ParamDefinition paramDefinition : operationDefinition.getParams()) {
            if (fixedValues.containsKey(paramDefinition.getName())) {
                continue;
            }
            StringBuilder part = new StringBuilder(paramDefinition.getName())
                    .append(" [").append(paramDefinition.getType()).append("]");
            TypeReference<?> effectiveType = parameterTypes.get(paramDefinition.getName());
            if (effectiveType == null && paramDefinition.getParamClass() != null) {
                effectiveType = typeReferenceOf(paramDefinition.getParamClass());
            }
            if (effectiveType != null) {
                part.append(" ").append(typeDisplayName(effectiveType));
            }
            if (paramDefinition.getDescription() != null && !paramDefinition.getDescription().isBlank()) {
                part.append(" ").append(paramDefinition.getDescription());
            }
            joiner.add(part.toString());
        }
        return joiner.toString();
    }

    private String buildFixedParameterSummary(ChenilePolymorphVariant polymorphVariant) {
        if (polymorphVariant == null || polymorphVariant.fixedParameterValues().isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ", " Fixed values: ", "");
        for (Map.Entry<String, Object> entry : polymorphVariant.fixedParameterValues().entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    private List<ChenileToolCallback.ChenileToolParameter> buildToolParameters(OperationDefinition operationDefinition,
                                                                               ChenilePolymorphVariant polymorphVariant) {
        Map<String, TypeReference<?>> parameterTypes = polymorphVariant == null ? Map.of() : polymorphVariant.parameterTypes();
        Map<String, Object> fixedValues = polymorphVariant == null ? Map.of() : polymorphVariant.fixedParameterValues();
        List<ChenileToolCallback.ChenileToolParameter> toolParameters = new ArrayList<>();
        for (ParamDefinition paramDefinition : operationDefinition.getParams()) {
            TypeReference<?> effectiveType = parameterTypes.get(paramDefinition.getName());
            if (effectiveType == null && paramDefinition.getParamClass() != null) {
                effectiveType = typeReferenceOf(paramDefinition.getParamClass());
            }
            if (effectiveType == null && operationDefinition.getInput() != null) {
                effectiveType = typeReferenceOf(operationDefinition.getInput());
            }
            toolParameters.add(new ChenileToolCallback.ChenileToolParameter(
                    paramDefinition.getName(),
                    toJackson2(effectiveType),
                    fixedValues.get(paramDefinition.getName())
            ));
        }
        return toolParameters;
    }

    /**
     * Converts a Jackson 3 TypeReference to a Jackson 2 TypeReference
     */
    public static <T> com.fasterxml.jackson.core.type.TypeReference<T> toJackson2(
            tools.jackson.core.type.TypeReference<T> jackson3Ref) {

        // Extract the underlying java.lang.reflect.Type
        final java.lang.reflect.Type type = jackson3Ref.getType();

        // Wrap it in a Jackson 2 TypeReference anonymous subclass
        return new com.fasterxml.jackson.core.type.TypeReference<T>() {
            @Override
            public java.lang.reflect.Type getType() {
                return type;
            }
        };
    }

    private String appendVariantSuffix(String baseName, ChenilePolymorphVariant polymorphVariant) {
        if (polymorphVariant == null || polymorphVariant.nameSuffix() == null || polymorphVariant.nameSuffix().isBlank()) {
            return baseName;
        }
        return baseName + "_" + polymorphVariant.nameSuffix();
    }

    private TypeReference<?> typeReferenceOf(Class<?> clazz) {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return clazz;
            }
        };
    }

    private String typeDisplayName(TypeReference<?> typeReference) {
        Type type = typeReference.getType();
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getTypeName();
        }
        return type.getTypeName();
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return toolCallbacks.toArray(ToolCallback[]::new);
    }


}
