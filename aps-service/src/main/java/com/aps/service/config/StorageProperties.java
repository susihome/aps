package com.aps.service.config;

import com.aps.domain.enums.StorageProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private StorageProvider provider = StorageProvider.MEMORY;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String tempBucket = "aps-temp";

    public StorageProvider getProvider() {
        return provider;
    }

    public void setProvider(StorageProvider provider) {
        this.provider = provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getTempBucket() {
        return tempBucket;
    }

    public void setTempBucket(String tempBucket) {
        this.tempBucket = tempBucket;
    }
}
