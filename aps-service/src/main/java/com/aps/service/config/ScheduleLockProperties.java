package com.aps.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.schedule.lock")
public class ScheduleLockProperties {

    private int renewFailureRetryCount = 3;

    public int getRenewFailureRetryCount() {
        return renewFailureRetryCount;
    }

    public void setRenewFailureRetryCount(int renewFailureRetryCount) {
        this.renewFailureRetryCount = renewFailureRetryCount;
    }
}
