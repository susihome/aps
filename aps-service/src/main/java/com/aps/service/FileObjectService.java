package com.aps.service;

import com.aps.domain.entity.FileObject;
import com.aps.domain.enums.FileObjectStatus;
import com.aps.domain.enums.FileVisibility;
import com.aps.service.config.StorageProperties;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.FileObjectRepository;
import com.aps.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileObjectService {

    private static final DateTimeFormatter OBJECT_KEY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileObjectRepository fileObjectRepository;
    private final FileStorageService fileStorageService;
    private final StorageProperties storageProperties;

    @Transactional
    public String storeTemporaryFile(String businessType, String fileName, byte[] content, Duration ttl, String contentType) {
        UUID fileId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        String objectKey = buildObjectKey(businessType, fileId, fileName, now);
        String bucketName = storageProperties.getTempBucket();

        fileStorageService.store(bucketName, objectKey, content, contentType);

        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setBusinessType(businessType);
        fileObject.setBucketName(bucketName);
        fileObject.setObjectKey(objectKey);
        fileObject.setFileName(fileName);
        fileObject.setContentType(contentType);
        fileObject.setFileSize(content.length);
        fileObject.setSha256(sha256(content));
        fileObject.setStorageProvider(storageProperties.getProvider());
        fileObject.setVisibility(FileVisibility.TEMPORARY);
        fileObject.setStatus(FileObjectStatus.READY);
        fileObject.setExpiresAt(now.plus(ttl));
        fileObjectRepository.save(fileObject);
        return fileId.toString();
    }

    @Transactional(readOnly = true)
    public StoredFile loadTemporaryFile(String token, String businessType) {
        FileObject fileObject = resolveActiveFile(token, businessType);
        byte[] content = fileStorageService.load(fileObject.getBucketName(), fileObject.getObjectKey());
        if (content == null) {
            throw new ResourceNotFoundException("文件不存在: " + token);
        }
        return new StoredFile(fileObject.getFileName(), content, fileObject.getContentType());
    }

    @Transactional
    public void cleanupExpiredFiles(String businessType) {
        List<FileObject> expiredFiles = fileObjectRepository.findTop100ByBusinessTypeAndExpiresAtBefore(
                businessType,
                LocalDateTime.now());
        for (FileObject expiredFile : expiredFiles) {
            fileStorageService.delete(expiredFile.getBucketName(), expiredFile.getObjectKey());
            expiredFile.setStatus(FileObjectStatus.EXPIRED);
            fileObjectRepository.delete(expiredFile);
        }
    }

    @Transactional
    public void deleteExpiredFile(String token, String businessType) {
        FileObject fileObject = resolveFile(token, businessType);
        fileStorageService.delete(fileObject.getBucketName(), fileObject.getObjectKey());
        fileObjectRepository.delete(fileObject);
    }

    private FileObject resolveActiveFile(String token, String businessType) {
        FileObject fileObject = resolveFile(token, businessType);
        if (fileObject.getExpiresAt().isAfter(LocalDateTime.now())) {
            return fileObject;
        }
        deleteExpiredFile(token, businessType);
        throw new ResourceNotFoundException("文件不存在: " + token);
    }

    private FileObject resolveFile(String token, String businessType) {
        UUID fileId;
        try {
            fileId = UUID.fromString(token);
        } catch (IllegalArgumentException exception) {
            throw new ResourceNotFoundException("文件不存在: " + token);
        }
        FileObject fileObject = fileObjectRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("文件不存在: " + token));
        if (!businessType.equals(fileObject.getBusinessType())) {
            throw new ResourceNotFoundException("文件不存在: " + token);
        }
        return fileObject;
    }

    private String buildObjectKey(String businessType, UUID fileId, String fileName, LocalDateTime now) {
        String normalizedFileName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        return businessType.toLowerCase() + "/" + OBJECT_KEY_DATE_FORMATTER.format(now) + "/" + fileId + "-" + normalizedFileName;
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("计算文件摘要失败", exception);
        }
    }

    public record StoredFile(String fileName, byte[] content, String contentType) {
    }
}
