package com.aps.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.schedule.task")
public class ScheduleTaskProperties {

    private long maxRetryCount = 3;
    private long retryDelayMs = 10_000;

    public long getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(long maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}
