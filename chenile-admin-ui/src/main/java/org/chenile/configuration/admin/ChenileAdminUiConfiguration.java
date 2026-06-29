package org.chenile.configuration.admin;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ChenileAdminUiConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/chenile/admin/**")
                .addResourceLocations(
                        "classpath:/static/chenile/admin/",
                        "classpath:/META-INF/resources/chenile/admin/"
                );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/chenile/admin", "/chenile/admin/index.html");
        registry.addRedirectViewController("/chenile/admin/", "/chenile/admin/index.html");
    }
}
