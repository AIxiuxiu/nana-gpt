package com.website.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ahl
 * @create 2023/6/8 18:03
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class TaskExecutorConfig implements AsyncConfigurer {

    public static final int cpuNum = Runtime.getRuntime().availableProcessors();

    /**
     * 异步线程池
     * @return
     */
    @Bean(name = "queueThreadPool")
    public ThreadPoolTaskExecutor queueThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //核心线程大小
        taskExecutor.setCorePoolSize(cpuNum);
        //最大线程大小
        taskExecutor.setMaxPoolSize(cpuNum * 2);
        // 设置队列容量
        taskExecutor.setQueueCapacity(500);
        // 设置线程空闲时间（秒）
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setAwaitTerminationSeconds(60);
        // 设置默认线程名称
        taskExecutor.setThreadNamePrefix("async-service-");
        // 设置拒绝策略
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        log.info("创建一个线程池 corePoolSize is [" + taskExecutor.getCorePoolSize() + "]" +
                " maxPoolSize is [" + taskExecutor.getMaxPoolSize() + "] " +
                " queueCapacity is [" + 500 + "]" +
                " keepAliveSeconds is [" + taskExecutor.getKeepAliveSeconds() + "]" +
                " namePrefix is [" + taskExecutor.getThreadNamePrefix() + "].");
        return taskExecutor;
    }

}
