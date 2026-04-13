package com.aps.service.storage;

public interface FileStorageService {

    void store(String bucketName, String objectKey, byte[] content, String contentType);

    byte[] load(String bucketName, String objectKey);

    void delete(String bucketName, String objectKey);
}
