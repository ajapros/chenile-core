package org.chenile.http.init;

import org.chenile.core.init.AnnotationChenileServiceInitializer;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.init.od.SpringMvcMappingProducer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * Uses a Spring controller with additional annotations to initiate a Chenile Service.
 * The controller must extend from ControllerSupport.
 */
public class HttpAnnotationChenileServiceInitializer extends AnnotationChenileServiceInitializer {
	private final SpringMvcMappingProducer mappingProducer;


	public HttpAnnotationChenileServiceInitializer(ApplicationContext ac, ChenileConfiguration chenileConfiguration){
		super(ac, chenileConfiguration);
		mappingProducer = new SpringMvcMappingProducer(ac);
	}

	@Override
	protected boolean supportsController(Object controller) {
		return controller.getClass().isAnnotationPresent(RestController.class);
	}

	@Override
	protected String defaultServiceName(String beanName, ChenileController controller) {
		return "_" + controller.value() + "_";
	}

	@Override
	protected void configureOperations(final Class<?> type, ChenileServiceDefinition csd) {
	    Class<?> klass = type;
	    while (klass != Object.class) {
	        for (final Method method : klass.getDeclaredMethods()) {
	            if (mappingProducer.supports(method)) mappingProducer.produceOperationDefinition(csd, method);
	        }
	        klass = klass.getSuperclass();
	    }
	}

}
