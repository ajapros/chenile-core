package org.chenile.configuration.http;



import org.chenile.core.model.ChenileConfiguration;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.http.init.AnnotationChenileServiceInitializer;
import org.chenile.http.init.AnnotationTrajectoryInitializer;
import org.chenile.http.init.HttpModuleBuilder;
import org.chenile.http.init.TrajectoryPostprocessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * Registers the beans that are needed for chenile-http
 */
@Configuration
@EnableWebMvc
public class UrlMappingConfiguration {
	
    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(){
        return new HttpModuleBuilder();
    }
    
    @Bean public ControllerSupport controllerSupport() {
    	return new ControllerSupport();
    }

    /**
     * This should run before all the initializers
     * @return an initializer that initiates a full fledged {@link org.chenile.core.model.ChenileConfiguration}
     */
    @Bean
    @Order(5)
    public AnnotationChenileServiceInitializer annotationChenileServiceInitializer(@Autowired ApplicationContext applicationContext,
                                                                                   ChenileConfiguration chenileConfiguration) {
    	return new AnnotationChenileServiceInitializer(applicationContext,chenileConfiguration);
    }
    
    @Bean
	public static TrajectoryPostprocessor trajectoryPostProcessor() {
		return new TrajectoryPostprocessor();
	}
	
	@Bean
	public AnnotationTrajectoryInitializer annotationTrajectoryInitializer() {
		return new AnnotationTrajectoryInitializer();
	}
    
}
