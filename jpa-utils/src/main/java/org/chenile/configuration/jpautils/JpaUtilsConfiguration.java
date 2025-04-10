package org.chenile.configuration.jpautils;

import org.chenile.core.context.PopulateContextContainer;
import org.chenile.jpautils.entity.JSR303ValidationInterceptor;
import org.chenile.utils.region.RegionToTrajectoryConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class JpaUtilsConfiguration {
    @Bean
    LocalValidatorFactoryBean localValidatorFactoryBean(){
        return new LocalValidatorFactoryBean();
    }

    @Bean
    JSR303ValidationInterceptor validationInterceptor(){
        return new JSR303ValidationInterceptor();
    }
}
