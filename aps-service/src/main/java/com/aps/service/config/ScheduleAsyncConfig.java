package com.aps.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ScheduleAsyncConfig {

    @Bean(name = "scheduleTaskExecutor", destroyMethod = "shutdown")
    public Executor scheduleTaskExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("schedule-task-", 0).factory());
    }

    @Bean(name = "scheduleLockRenewalExecutor", destroyMethod = "shutdown")
    public ScheduledExecutorService scheduleLockRenewalExecutor() {
        return Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().name("schedule-lock-renewal-", 0).daemon(true).factory());
    }
}
