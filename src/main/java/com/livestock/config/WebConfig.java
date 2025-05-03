package com.livestock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL /images/produtos/** → pasta uploads/images/produtos/ do disco
        registry.addResourceHandler("/images/produtos/**")
                .addResourceLocations("file:uploads/images/produtos/");
    }
}
