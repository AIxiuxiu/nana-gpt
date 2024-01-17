package com.website;

import com.website.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.SocketException;

@Slf4j
@EnableScheduling
@EnableEurekaClient
@SpringBootApplication
@ServletComponentScan
@EnableAsync
@EnableTransactionManagement
public class P5WGPTApplication {


	public static void main(String[] args) throws SocketException {
		ConfigurableApplicationContext application = SpringApplication.run(P5WGPTApplication.class, args);
		Environment env = application.getEnvironment();
		String ip = IpUtil.getRealIp();
		String port = env.getProperty("server.port");
		String path = env.getProperty("server.servlet.context-path");
		if (StringUtils.isEmpty(path)) {
			path = "/";
		}
		log.info("Application  is running! Access URLs:\n\t" +
				"Local访问网址: \t\thttp://localhost:" + port + path + "\n\t" +
				"External访问网址: \thttp://" + ip + ":" + port + path + "\n\t" +
				"Swagger访问网址: \thttp://" + ip + ":" + port + path + "doc.html" + "\n\t");
	}

}
