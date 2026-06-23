package org.chenile.configuration.admin;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ChenileAdminUiConfiguration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/chenile/admin", "/chenile/admin/");
        registry.addViewController("/chenile/admin/").setViewName("forward:/chenile/admin/index.html");
    }
}
