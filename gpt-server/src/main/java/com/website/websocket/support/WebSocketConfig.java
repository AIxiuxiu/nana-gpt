package com.website.websocket.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author ahl
 * @desc
 * @create 2023/1/11 09:14
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        //这个对象只有服务器是tomcat的时候才需要配置
        return new ServerEndpointExporter();
    }

}