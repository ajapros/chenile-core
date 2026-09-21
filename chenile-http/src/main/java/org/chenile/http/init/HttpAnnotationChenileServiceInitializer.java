package org.chenile.http.init;

import org.chenile.core.init.AnnotationChenileServiceInitializer;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.init.od.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * Uses a Spring controller with additional annotations to initiate a Chenile Service.
 * The controller must extend from ControllerSupport.
 */
public class HttpAnnotationChenileServiceInitializer extends AnnotationChenileServiceInitializer {
	private DeleteMappingProducer deleteMappingProducer; 
	private GetMappingProducer getMappingProducer ;
	private PatchMappingProducer patchMappingProducer;
	private PostMappingProducer postMappingProducer;
	private PutMappingProducer putMappingProducer;


	public HttpAnnotationChenileServiceInitializer(ApplicationContext ac, ChenileConfiguration chenileConfiguration){
		super(ac, chenileConfiguration);
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
	protected void init() {
		deleteMappingProducer = new DeleteMappingProducer(applicationContext);
		getMappingProducer = new GetMappingProducer(applicationContext);
		patchMappingProducer = new PatchMappingProducer(applicationContext);
		postMappingProducer = new PostMappingProducer(applicationContext);
		putMappingProducer = new PutMappingProducer(applicationContext);
		super.init();
	}

	@Override
	protected void configureOperations(final Class<?> type, ChenileServiceDefinition csd) {
	    Class<?> klass = type;
	    while (klass != Object.class) { // need to iterate thought hierarchy in order to retrieve methods from above the current instance
	        // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
	        for (final Method method : klass.getDeclaredMethods()) {
	            if (method.isAnnotationPresent(GetMapping.class)) {
	            	getMappingProducer.produceOperationDefinition(csd, method);
	            }else if (method.isAnnotationPresent(DeleteMapping.class)) {
	            	deleteMappingProducer.produceOperationDefinition(csd, method);
	            }else if (method.isAnnotationPresent(PutMapping.class)) {
	            	putMappingProducer.produceOperationDefinition(csd, method);
	            }else if (method.isAnnotationPresent(PostMapping.class)) {
	            	postMappingProducer.produceOperationDefinition(csd, method);
	            }else if (method.isAnnotationPresent(PatchMapping.class)) {
	            	patchMappingProducer.produceOperationDefinition(csd, method);
	            }
	        }
	        // move to the upper class in the hierarchy in search for more methods
	        klass = klass.getSuperclass();
	    }
	}

}
