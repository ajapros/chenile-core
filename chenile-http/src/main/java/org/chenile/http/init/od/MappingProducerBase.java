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
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/** HTTP binding specialization of the core operation producer. */
public abstract class MappingProducerBase extends OperationDefinitionProducerBase {
	private static final Logger logger = LoggerFactory.getLogger(MappingProducerBase.class);

	public MappingProducerBase(ApplicationContext applicationContext) {
		super(applicationContext);
	}

	@Override
	protected void configureTransport(OperationDefinition operation, Method method) {
		String[] urls = url(method);
		operation.setUrl(urls != null && urls.length > 0 ? urls[0] : null);
		operation.setHttpMethod(httpMethod());
		String[] consumes = consumes(method);
		if (consumes != null && consumes.length > 0 && !consumes[0].isEmpty()) {
			operation.setConsumes(MimeType.valueOf(consumes[0]));
		}
		String[] produces = produces(method);
		if (produces != null && produces.length > 0 && !produces[0].isEmpty()) {
			operation.setProduces(MimeType.valueOf(produces[0]));
		}
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

	protected abstract String[] url(Method method);
	protected abstract HTTPMethod httpMethod();
	protected abstract String[] consumes(Method method);
	protected abstract String[] produces(Method method);
}
