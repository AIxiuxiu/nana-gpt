package com.website.config;

import com.website.controller.handler.AuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置拦截器
 * @author ahl
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = new ArrayList<>();
        // 开放knife4j
        excludePaths.add("/doc.html");
        excludePaths.add("/service-worker.js");
        excludePaths.add("/swagger-resources");
        excludePaths.add("/error");

        registry.addInterceptor(authenticationInterceptor())
                // 需要拦截的请求，同时通过判断是否有 @PassToken 来开放请求
                .addPathPatterns("/**")
        .excludePathPatterns(excludePaths);
    }

    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }
}