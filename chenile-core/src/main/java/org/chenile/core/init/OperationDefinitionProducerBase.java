package org.chenile.core.init;

import org.chenile.core.annotation.ChenileAnnotation;
import org.chenile.core.annotation.ChenileBody;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.event.SubscribeTo;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.http.annotation.BodyTypeSelector;
import org.chenile.http.annotation.EventsSubscribedTo;
import org.chenile.http.annotation.InterceptedBy;
import org.chenile.owiz.Command;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds transport-neutral Chenile operation metadata from a Java method.
 * Transport adapters extend this class only to add their own bindings.
 */
public class OperationDefinitionProducerBase {
	protected final ApplicationContext applicationContext;

	public OperationDefinitionProducerBase(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void produceOperationDefinition(ChenileServiceDefinition serviceDefinition, Method method) {
		produceOperationDefinition(serviceDefinition, method, method.getName());
	}

	public void produceOperationDefinition(ChenileServiceDefinition serviceDefinition, Method method,
			String operationName) {
		OperationDefinition operation = new OperationDefinition();
		operation.setName(operationName);
		operation.setMethodName(method.getName());
		configureTransport(operation, method);
		populateOutputTypes(operation, method);
		populateParams(method, operation);
		serviceDefinition.getOperations().add(operation);
	}

	/** Hook for HTTP, messaging, or other adapters to add transport bindings. */
	protected void configureTransport(OperationDefinition operation, Method method) {
	}

	protected void populateOutputTypes(OperationDefinition operation, Method method) {
		operation.setOutputAsParameterizedReference(ParameterizedTypeReference.forType(method.getGenericReturnType()));
		operation.setOutput(method.getReturnType());
	}

	protected void populateParams(Method method, OperationDefinition operation) {
		processOperationAnnotations(method, operation);
		List<ParamDefinition> params = new ArrayList<>();
		for (Parameter parameter : method.getParameters()) {
			if (!includeParameter(parameter)) continue;
			ParamDefinition param = new ParamDefinition();
			param.setName(parameter.getName());
			param.setParamType(parameter.getParameterizedType());
			param.setType(isBodyParameter(parameter) ? HttpBindingType.BODY : HttpBindingType.HEADER);
			if (param.getType() == HttpBindingType.BODY) operation.setInput(parameter.getType());
			customizeParam(operation, param, parameter);
			params.add(param);
		}
		operation.setParams(params);
	}

	/** Hook for transports that carry framework-only method parameters. */
	protected boolean includeParameter(Parameter parameter) {
		return true;
	}

	protected boolean isBodyParameter(Parameter parameter) {
		return parameter.isAnnotationPresent(ChenileBody.class);
	}

	/** Hook for transport-specific parameter annotations. */
	protected void customizeParam(OperationDefinition operation, ParamDefinition param, Parameter parameter) {
	}

	protected void processOperationAnnotations(Method method, OperationDefinition operation) {
		processInterceptedBy(method, operation);
		processBodyTypeSelector(method, operation);
		processEventsSubscribedTo(method, operation);
		collectChenileAnnotations(method, operation);
	}

	@SuppressWarnings("unchecked")
	private void processInterceptedBy(Method method, OperationDefinition operation) {
		InterceptedBy interceptedBy = method.getAnnotation(InterceptedBy.class);
		if (interceptedBy == null || interceptedBy.value() == null) return;
		List<Command<ChenileExchange>> commands = new ArrayList<>();
		for (String interceptorName : interceptedBy.value()) {
			commands.add((Command<ChenileExchange>) applicationContext.getBean(interceptorName));
		}
		operation.setInterceptorComponentNames(Arrays.asList(interceptedBy.value()));
		operation.setInterceptorCommands(commands);
	}

	private void processBodyTypeSelector(Method method, OperationDefinition operation) {
		BodyTypeSelector selector = method.getAnnotation(BodyTypeSelector.class);
		if (selector == null || selector.value() == null) return;
		operation.setBodyTypeSelectorComponentNames(selector.value());
		operation.setBodyTypeSelector(AbstractServiceInitializer.constructBodyTypeInterceptorsChain(
				selector.value(), applicationContext));
	}

	private void processEventsSubscribedTo(Method method, OperationDefinition operation) {
		Set<String> subscriptions = new HashSet<>();
		EventsSubscribedTo eventsSubscribedTo = method.getAnnotation(EventsSubscribedTo.class);
		if (eventsSubscribedTo != null && eventsSubscribedTo.value() != null) {
			subscriptions.addAll(Arrays.asList(eventsSubscribedTo.value()));
		}
		SubscribeTo subscribeTo = method.getAnnotation(SubscribeTo.class);
		if (subscribeTo != null) subscriptions.add(subscribeTo.value());
		if (!subscriptions.isEmpty()) operation.setEventSubscribedTo(subscriptions);
	}

	protected void collectChenileAnnotations(Method method, OperationDefinition operation) {
		for (Annotation annotation : method.getAnnotations()) {
			Class<? extends Annotation> type = annotation.annotationType();
			if (!type.isAnnotationPresent(ChenileAnnotation.class)) continue;
			String name = type.getName().substring(type.getName().lastIndexOf('.') + 1);
			Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
			operation.putExtension(name, attributes);
			operation.putExtensionAsAnnotation(type, annotation);
		}
	}
}
