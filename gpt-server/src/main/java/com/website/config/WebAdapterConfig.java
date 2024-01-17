package com.website.config;

import com.website.controller.handler.RestAndViewMethodReturnValueHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebAdapterConfig implements WebMvcConfigurer {

    @Bean
    public RestAndViewMethodReturnValueHandler restAndViewMethodReturnValueHandler() {
        return new RestAndViewMethodReturnValueHandler();
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        returnValueHandlers.add(restAndViewMethodReturnValueHandler());
    }

}