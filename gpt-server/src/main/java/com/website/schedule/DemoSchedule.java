package com.website.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Slf4j
@Component
public class DemoSchedule {

//    @Scheduled(cron = "0 0 23 */1 * ?")
    public void demoSchedule() {
        log.info("定时任务");
    }
}
