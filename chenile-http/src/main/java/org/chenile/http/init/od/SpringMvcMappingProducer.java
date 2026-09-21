package org.chenile.http.init.od;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.exception.ServerException;
import org.chenile.core.init.OperationDefinitionProducerBase;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.MimeType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.annotation.ChenileResponseCodes;
import org.chenile.http.annotation.ParamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Produces Chenile HTTP operation metadata from Spring MVC mappings. Spring's
 * composed mapping annotations, such as {@code GetMapping}, are resolved as a
 * merged {@link RequestMapping}, so one producer covers every supported verb.
 */
public class SpringMvcMappingProducer extends OperationDefinitionProducerBase {
	private static final Logger logger = LoggerFactory.getLogger(SpringMvcMappingProducer.class);

	public SpringMvcMappingProducer(ApplicationContext applicationContext) {
		super(applicationContext);
	}

	public boolean supports(Method method) {
		return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
	}

	@Override
	protected void configureTransport(OperationDefinition operation, Method method) {
		RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
		if (mapping == null) return;
		String[] paths = mapping.path().length > 0 ? mapping.path() : mapping.value();
		if (paths.length > 1) {
			throw new IllegalArgumentException("Chenile operations support one URL: " + method);
		}
		operation.setUrl(paths.length == 0 ? null : paths[0]);
		operation.setHttpMethod(toChenileHttpMethod(mapping.method(), method));
		setMimeType(mapping.consumes(), operation::setConsumes, "consumes", method);
		setMimeType(mapping.produces(), operation::setProduces, "produces", method);
	}

	private HTTPMethod toChenileHttpMethod(RequestMethod[] methods, Method javaMethod) {
		if (methods.length != 1) {
			throw new IllegalArgumentException("Chenile operations require exactly one HTTP method: " + javaMethod);
		}
		return switch (methods[0]) {
			case GET -> HTTPMethod.GET;
			case POST -> HTTPMethod.POST;
			case PUT -> HTTPMethod.PUT;
			case PATCH -> HTTPMethod.PATCH;
			case DELETE -> HTTPMethod.DELETE;
			default -> throw new IllegalArgumentException("Unsupported Chenile HTTP method " + methods[0] + ": " + javaMethod);
		};
	}

	private void setMimeType(String[] values, java.util.function.Consumer<MimeType> setter,
			String attribute, Method method) {
		if (values.length > 1) {
			throw new IllegalArgumentException("Chenile operations support one " + attribute + " value: " + method);
		}
		if (values.length == 1 && !values[0].isEmpty()) setter.accept(MimeType.from(values[0]));
	}

	@Override
	protected void processOperationAnnotations(Method method, OperationDefinition operation) {
		super.processOperationAnnotations(method, operation);
		ChenileResponseCodes responseCodes = method.getAnnotation(ChenileResponseCodes.class);
		if (responseCodes != null) {
			operation.setSuccessHttpStatus(responseCodes.success());
			operation.setWarningHttpStatus(responseCodes.warning());
		}
	}

	@Override
	protected boolean includeParameter(Parameter parameter) {
		return !HttpServletRequest.class.isAssignableFrom(parameter.getType());
	}

	@Override
	protected boolean isBodyParameter(Parameter parameter) {
		return super.isBodyParameter(parameter) || parameter.isAnnotationPresent(RequestBody.class);
	}

	@Override
	protected void customizeParam(OperationDefinition operation, ParamDefinition param, Parameter parameter) {
		if (parameter.isAnnotationPresent(ParamInfo.class)) {
			ParamInfo info = parameter.getAnnotation(ParamInfo.class);
			param.setName(info.name());
			param.setDescription(info.description());
		}
		if (parameter.isAnnotationPresent(ChenileParamType.class)) {
			ChenileParamType type = parameter.getAnnotation(ChenileParamType.class);
			if (type.value() != null) {
				param.setParamClass(type.value());
				if (operation.getInput().equals(String.class)) operation.setInput(type.value());
			}
		}
		logger.info("The Parameterized type for {} is {}", operation.getName(), param.getParamType());
	}

	@Override
	protected void populateOutputTypes(OperationDefinition operation, Method method) {
		ResolvableType type = ResolvableType.forMethodReturnType(method).getGeneric().getGeneric();
		operation.setOutputAsParameterizedReference(ParameterizedTypeReference.forType(type.getType()));
		operation.setOutput(type.getRawClass());
	}

	@Override
	protected void populateParams(Method method, OperationDefinition operation) {
		if (method.getParameterCount() == 0 ||
				!HttpServletRequest.class.isAssignableFrom(method.getParameterTypes()[0])) {
			throw new ServerException(org.chenile.core.errorcodes.ErrorCodes.INVALID_CONTROLLER_ARGS.getSubError(),
					new Object[]{operation.getServiceName(), method.getName()});
		}
		super.populateParams(method, operation);
	}
}
