package com.gourmich.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/")
                .setViewName("forward:/index.html");

        registry.addViewController("/{spring:(?!api|assets|media)[^\\.]*}")
                .setViewName("forward:/index.html");

        registry.addViewController("/{spring:(?!api|assets|media)[^\\.]*}/**")
                .setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*.js", "/*.css", "/*.html")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/media/**")
                .addResourceLocations("classpath:/static/media/")
                .setCachePeriod(3600);
    }


}