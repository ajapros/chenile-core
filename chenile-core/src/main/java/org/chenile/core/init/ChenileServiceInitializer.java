package org.chenile.core.init;

import org.chenile.core.model.ChenileConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Concrete class for initializing services from Json resources
 */
public class ChenileServiceInitializer extends AbstractServiceInitializer{
	
	private final Resource[] chenileServiceJsonResources;

	public ChenileServiceInitializer(Resource[] chenileServiceJsonResources,ApplicationContext ac, ChenileConfiguration chenileConfiguration) {
        super(chenileConfiguration,ac);
        this.chenileServiceJsonResources = chenileServiceJsonResources;
	}

	@Override
	public void init() throws Exception {
		for(Resource chenileResource: chenileServiceJsonResources ) {
			registerService(chenileResource);
		}
	}

}
