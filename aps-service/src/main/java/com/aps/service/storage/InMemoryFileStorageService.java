package com.aps.service.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "MEMORY", matchIfMissing = true)
public class InMemoryFileStorageService implements FileStorageService {

    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    @Override
    public void store(String bucketName, String objectKey, byte[] content, String contentType) {
        storage.put(storageKey(bucketName, objectKey), content.clone());
    }

    @Override
    public byte[] load(String bucketName, String objectKey) {
        byte[] content = storage.get(storageKey(bucketName, objectKey));
        return content == null ? null : content.clone();
    }

    @Override
    public void delete(String bucketName, String objectKey) {
        storage.remove(storageKey(bucketName, objectKey));
    }

    private String storageKey(String bucketName, String objectKey) {
        return bucketName + ":" + objectKey;
    }
}
