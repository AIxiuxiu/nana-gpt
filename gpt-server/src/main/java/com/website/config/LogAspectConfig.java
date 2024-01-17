package com.website.config;

import com.website.aop.LogAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="log.aspect",name="enable", havingValue="true", matchIfMissing=true)
public class LogAspectConfig {

    @Bean
    public LogAspect buildLogAspect() {
        return new LogAspect();
    }
}
