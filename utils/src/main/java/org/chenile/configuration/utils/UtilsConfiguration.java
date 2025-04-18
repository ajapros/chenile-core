package org.chenile.configuration.utils;

import org.chenile.core.context.PopulateContextContainer;
import org.chenile.utils.region.RegionToTrajectoryConverter;
import org.chenile.utils.stream.Looper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfiguration {
	
	@Bean 
	public RegionToTrajectoryConverter regionToTrajectoryConverter() {
		return new RegionToTrajectoryConverter();
	}
	@Bean public Looper looper() { return new Looper();}
}
