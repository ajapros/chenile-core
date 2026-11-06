package org.chenile.core.transform;

import org.chenile.base.exception.ServerException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.interceptors.BaseChenileInterceptor;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.util.convert.ChenileTypeUtils;
import org.chenile.owiz.Command;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.Type;

/**
 * Selects the target Java type for the request body and stores it in
 * {@link ChenileExchange#getBodyType()} for the following {@code Transformer} interceptor.
 *
 * <p>Selection runs in this order:
 * <ol>
 *   <li>An operation-level body type selector, if configured.</li>
 *   <li>Otherwise, a service-level body type selector, if configured.</li>
 *   <li>Otherwise, the static operation and BODY parameter metadata, resolved by
 *       {@link #setDefaultBodyType(ChenileExchange)}.</li>
 * </ol>
 * A registered selector owns the decision; the static fallback is used only when neither
 * selector is present. On the default path, the chosen {@link Type} is kept in a
 * {@link TypeReference} so that parameterized request bodies, such as {@code List<MyRequest>},
 * retain their element type through JSON conversion. A configured selector is responsible for
 * setting the exchange body type. The following {@code Transformer} uses this type to convert
 * raw JSON, decoded maps, decoded collections, and JSON nodes before service arguments are
 * bound. Already-compatible decoded values are preserved, and ordinary Java object bodies are
 * passed through unchanged.
 *
 * @author Raja Shankar Kolluru
 */

public class TransformationClassSelector extends BaseChenileInterceptor {

    @Override
    public void doPreProcessing(ChenileExchange exchange) {
        // Has a body type selector been registered against the service definition or operation definition
        // if so invoke it
        Command<ChenileExchange> bts = obtainBodyTypeSelector(exchange);
        if (bts != null) {
            try {
                bts.execute(exchange);
            } catch (Exception e) {
                throw new ServerException(ErrorCodes.BODY_TYPE_SELECTOR_ERROR.getSubError(), new Object[]{});
            }
        } else
            setDefaultBodyType(exchange);
    }

    private Command<ChenileExchange> obtainBodyTypeSelector(ChenileExchange exchange) {
        ChenileServiceDefinition sd = exchange.getServiceDefinition();
        OperationDefinition od = exchange.getOperationDefinition();
        if (od.getBodyTypeSelector() != null)
            return od.getBodyTypeSelector();
        else
            return sd.getBodyTypeSelector();
    }

    private void setDefaultBodyType(ChenileExchange exchange) {
        OperationDefinition od = exchange.getOperationDefinition();
        // Start with the operation's configured input. It represents the intended request
        // model and can be more specific than the Java method's parameter type. For example,
        // a workflow method may accept StateEntity while this operation is configured for Issue.
        Type inputType = od.getInput();
        if (od.getParams() != null) {
            // ParamDefinition preserves the complete reflective Type, while operation.input
            // is only a Class. The BODY parameter can therefore restore generic information
            // that Class cannot represent, such as List<Payload>.
            Type bodyParameterType = od.getParams().stream()
                    .filter(param -> param.getType() == HttpBindingType.BODY)
                    .map(param -> param.getParamType())
                    .filter(type -> type != null)
                    .findFirst()
                    .orElse(null);
            if (bodyParameterType != null) {
                Class<?> bodyRawType = ChenileTypeUtils.toRawClass(bodyParameterType);
                Class<?> operationInputType = od.getInput();
                // Use parameter metadata when it adds detail to the same input type, or when
                // operation.input is absent/defaulted. If the raw types differ, retain the
                // operation's explicit model rather than selecting a different method-parameter
                // type, which might be abstract or otherwise unsuitable for deserialization.
                if (operationInputType == null
                        || operationInputType == String.class
                        || operationInputType.equals(bodyRawType)) {
                    inputType = bodyParameterType;
                }
            }
        }
        if (inputType != null) {
            TypeReference<?> ref = convertTypeToTypeReference(inputType);
            exchange.setBodyType(ref);
        }
    }

    public static TypeReference<?> convertClassToTypeReference(Class<?> clazz) {
        return convertTypeToTypeReference(clazz);
    }

    public static TypeReference<?> convertTypeToTypeReference(Type type) {
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }
}
