package com.website.service.cache;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value=1)
public class StartupRunnerCache implements CommandLineRunner {

    private final CacheSchedule kbInfoSchedule;

    public StartupRunnerCache(CacheSchedule kbInfoSchedule) {
        this.kbInfoSchedule = kbInfoSchedule;
    }

    @Override
    public void run(String... var1) {
        kbInfoSchedule.getKbInfoSchedule();
    }
}
