package org.chenile.configuration.core;

import java.io.IOException;
import java.util.Properties;

import org.chenile.core.model.ChenileConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


/**
 * This file exists for transferring version information to ChenileConfiguration.
 * This is because we wanted two configuration files to read property files - 
 * one for version.txt and the other for chenile.properties
 * @author Raja Shankar Kolluru
 *
 */
@Configuration
public class ChenileVersionConfiguration implements InitializingBean{
	
	@Autowired ChenileConfiguration chenileConfiguration;
	private static final String VERSION_PATTERN = "classpath*:**/*version.txt";

	@Override
	public void afterPropertiesSet() throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(VERSION_PATTERN);
		for (Resource resource : resources) {
			loadVersions(resource);
		}
	}

	private void loadVersions(Resource resource) throws IOException {
		Properties properties = new Properties();
		try (var inputStream = resource.getInputStream()) {
			properties.load(inputStream);
		}
		for (String key : properties.stringPropertyNames()) {
			chenileConfiguration.addVersion(key, properties.getProperty(key));
		}
	}
}
