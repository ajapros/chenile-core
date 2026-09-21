package org.chenile.core.init;

import org.chenile.base.exception.ServerException;
import org.chenile.core.annotation.ChenileAnnotation;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.service.HealthChecker;
import org.chenile.core.util.MethodUtils;
import org.chenile.http.annotation.ChenileAdditionalAttribute;
import org.chenile.http.annotation.ChenileController;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers non-HTTP Chenile controllers. HTTP controllers are deliberately
 * left to chenile-http, which adds Spring MVC-specific operation metadata.
 */
public class AnnotationChenileServiceInitializer extends AbstractServiceInitializer {
	private final OperationDefinitionProducerBase operationDefinitionProducer;

	public AnnotationChenileServiceInitializer(ApplicationContext applicationContext,
			ChenileConfiguration chenileConfiguration) {
		super(chenileConfiguration, applicationContext);
		operationDefinitionProducer = new OperationDefinitionProducerBase(applicationContext);
	}

	@Override
	protected void init() {
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ChenileController.class);
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			Object controller = entry.getValue();
			if (!supportsController(controller)) continue;
			ChenileController annotation = controller.getClass().getAnnotation(ChenileController.class);
			ChenileServiceDefinition serviceDefinition = buildServiceDefinition(entry.getKey(), controller, annotation);
			configureOperations(controller.getClass(), serviceDefinition);
			Class<?> interfaceClass = annotation.interfaceClass();
			if (interfaceClass == Object.class) interfaceClass = computeInterfaceClass(serviceDefinition);
			serviceDefinition.setInterfaceClass(interfaceClass);
			registerService(serviceDefinition);
		}
	}

	/** Lets a transport adapter select the controller type it owns. */
	protected boolean supportsController(Object controller) {
		return !isRestController(controller);
	}

	/**
	 * Resolves the default service-reference bean name when the controller has
	 * not supplied one explicitly. The HTTP adapter retains its historical
	 * underscore-delimited convention by overriding this hook.
	 */
	protected String defaultServiceName(String beanName, ChenileController annotation) {
		return beanName;
	}

	protected ChenileServiceDefinition buildServiceDefinition(String beanName, Object controller,
			ChenileController annotation) {
		ChenileServiceDefinition serviceDefinition = new ChenileServiceDefinition();
		serviceDefinition.setId(annotation.value());
		serviceDefinition.setMonolithName(serviceConfiguration.getMonolithName());
		serviceDefinition.setRegisterInServiceRegistry(annotation.registerInServiceRegistry());

		String serviceName = annotation.serviceName();
		if (serviceName.isEmpty()) serviceName = defaultServiceName(beanName, annotation);
		serviceDefinition.setName(serviceName);
		Object serviceReference = lookup(serviceName);
		if (serviceReference == null) {
			throw new ServerException(ErrorCodes.MISSING_SERVICE_REFERENCE.getSubError(),
					new Object[]{annotation.value()});
		}
		serviceDefinition.setServiceReference(serviceReference);

		String serviceModule = annotation.serviceModule();
		serviceDefinition.setServiceModule(serviceModule.isEmpty() ? annotation.value() : serviceModule);
		if (!annotation.bluePrintName().isEmpty()) serviceDefinition.setBluePrintName(annotation.bluePrintName());
		Map<String, String> additionalAttributes = new HashMap<>();
		for (ChenileAdditionalAttribute additionalAttribute : annotation.additionalAttributes()) {
			additionalAttributes.put(additionalAttribute.key(), additionalAttribute.value());
		}
		serviceDefinition.setAdditionalAttributes(additionalAttributes);

		String healthCheckerName = annotation.healthCheckerName();
		if (healthCheckerName.isEmpty()) healthCheckerName = annotation.value() + "HealthChecker";
		Object healthChecker = lookup(healthCheckerName);
		if (healthChecker != null) {
			serviceDefinition.setHealthCheckerName(healthCheckerName);
			serviceDefinition.setHealthChecker((HealthChecker) healthChecker);
		}
		String mockName = annotation.mockName();
		if (mockName.isEmpty()) mockName = annotation.value() + "Mock";
		Object mockReference = lookup(mockName);
		if (mockReference != null) {
			serviceDefinition.setMockName(mockName);
			serviceDefinition.setMockServiceReference(mockReference);
		}

		serviceDefinition.setOperations(new ArrayList<>());
		collectChenileAnnotations(controller, serviceDefinition);
		return serviceDefinition;
	}

	/** Builds transport-neutral operations. Transport adapters override this. */
	protected void configureOperations(Class<?> type, ChenileServiceDefinition serviceDefinition) {
		Class<?> current = type;
		while (current != Object.class) {
			for (Method method : current.getDeclaredMethods()) {
				org.chenile.core.annotation.ChenileOperation operation =
						method.getAnnotation(org.chenile.core.annotation.ChenileOperation.class);
				if (operation != null) {
					String operationName = operation.value().isBlank() ? method.getName() : operation.value();
					operationDefinitionProducer.produceOperationDefinition(serviceDefinition, method, operationName);
				}
			}
			current = current.getSuperclass();
		}
	}

	protected boolean isRestController(Object controller) {
		for (Annotation annotation : controller.getClass().getAnnotations()) {
			if (annotation.annotationType().getName().equals("org.springframework.web.bind.annotation.RestController")) {
				return true;
			}
		}
		return false;
	}

	protected Class<?> computeInterfaceClass(ChenileServiceDefinition serviceDefinition) {
		for (Class<?> candidate : ClassUtils.getAllInterfaces(serviceDefinition.getServiceReference())) {
			boolean found = true;
			for (OperationDefinition operation : serviceDefinition.getOperations()) {
				if (MethodUtils.computeMethod(candidate, operation) == null) {
					found = false;
					break;
				}
			}
			if (found) return candidate;
		}
		return null;
	}

	protected Object lookup(String name) {
		try {
			return applicationContext.getBean(name);
		} catch (NoSuchBeanDefinitionException exception) {
			return null;
		}
	}

	protected void collectChenileAnnotations(Object source, ChenileServiceDefinition serviceDefinition) {
		for (Annotation annotation : source.getClass().getAnnotations()) {
			Class<? extends Annotation> type = annotation.annotationType();
			if (type.isAnnotationPresent(ChenileAnnotation.class)) {
				serviceDefinition.putExtension(simpleName(type), AnnotationUtils.getAnnotationAttributes(annotation));
				serviceDefinition.putExtensionAsAnnotation(type, annotation);
			}
		}
	}

	protected String simpleName(Class<?> type) {
		return type.getName().substring(type.getName().lastIndexOf('.') + 1);
	}
}
