package com.happymarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import config.SessionInterceptor;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(@SuppressWarnings("null") ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/subir").setViewName("subir");
        registry.addViewController("/publicar").setViewName("publicar");
    }

    @Override
    public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
        
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:/templates/");
        
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }

    @Override
    public void addInterceptors(@SuppressWarnings("null") InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor());
    }
}