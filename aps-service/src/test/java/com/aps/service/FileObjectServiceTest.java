package com.aps.service;

import com.aps.domain.entity.FileObject;
import com.aps.domain.enums.FileObjectStatus;
import com.aps.domain.enums.StorageProvider;
import com.aps.service.config.StorageProperties;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.FileObjectRepository;
import com.aps.service.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("文件对象服务测试")
class FileObjectServiceTest {

    @Mock
    private FileObjectRepository fileObjectRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private FileObjectService fileObjectService;

    @Test
    @DisplayName("存储临时文件时应上传文件并保存元数据")
    void storeTemporaryFile_shouldUploadAndPersistMetadata() {
        byte[] content = "error".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        AtomicReference<FileObject> savedFileObject = new AtomicReference<>();
        when(storageProperties.getTempBucket()).thenReturn("aps-temp");
        when(storageProperties.getProvider()).thenReturn(StorageProvider.MEMORY);
        when(fileObjectRepository.save(any(FileObject.class))).thenAnswer(invocation -> {
            FileObject fileObject = invocation.getArgument(0);
            savedFileObject.set(fileObject);
            return fileObject;
        });

        String token = fileObjectService.storeTemporaryFile(
                "MATERIAL_IMPORT_ERROR",
                "materials-import-errors.csv",
                content,
                Duration.ofHours(24),
                "text/csv");

        assertThat(token).isEqualTo(savedFileObject.get().getId().toString());
        assertThat(savedFileObject.get().getBusinessType()).isEqualTo("MATERIAL_IMPORT_ERROR");
        assertThat(savedFileObject.get().getStatus()).isEqualTo(FileObjectStatus.READY);
        verify(fileStorageService).store(eq("aps-temp"), any(String.class), eq(content), eq("text/csv"));
    }

    @Test
    @DisplayName("加载未过期文件时应返回文件内容")
    void loadTemporaryFile_shouldReturnStoredFile() {
        UUID fileId = UUID.randomUUID();
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setBusinessType("MATERIAL_IMPORT_ERROR");
        fileObject.setBucketName("aps-temp");
        fileObject.setObjectKey("materials/error/test.csv");
        fileObject.setFileName("test.csv");
        fileObject.setContentType("text/csv");
        fileObject.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(fileObjectRepository.findById(fileId)).thenReturn(Optional.of(fileObject));
        when(fileStorageService.load("aps-temp", "materials/error/test.csv"))
                .thenReturn("content".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        FileObjectService.StoredFile storedFile =
                fileObjectService.loadTemporaryFile(fileId.toString(), "MATERIAL_IMPORT_ERROR");

        assertThat(storedFile.fileName()).isEqualTo("test.csv");
        assertThat(new String(storedFile.content(), java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("content");
    }

    @Test
    @DisplayName("加载过期文件时应删除并抛出未找到异常")
    void loadTemporaryFile_whenExpired_shouldDeleteAndThrow() {
        UUID fileId = UUID.randomUUID();
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setBusinessType("MATERIAL_IMPORT_ERROR");
        fileObject.setBucketName("aps-temp");
        fileObject.setObjectKey("materials/error/test.csv");
        fileObject.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(fileObjectRepository.findById(fileId)).thenReturn(Optional.of(fileObject));

        assertThatThrownBy(() -> fileObjectService.loadTemporaryFile(fileId.toString(), "MATERIAL_IMPORT_ERROR"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("文件不存在");

        verify(fileStorageService).delete("aps-temp", "materials/error/test.csv");
        verify(fileObjectRepository).delete(fileObject);
    }

    @Test
    @DisplayName("清理过期文件时应删除对象存储内容和元数据")
    void cleanupExpiredFiles_shouldDeleteExpiredObjects() {
        FileObject expiredFile = new FileObject();
        expiredFile.setId(UUID.randomUUID());
        expiredFile.setBusinessType("MATERIAL_IMPORT_ERROR");
        expiredFile.setBucketName("aps-temp");
        expiredFile.setObjectKey("materials/error/test.csv");
        when(fileObjectRepository.findTop100ByBusinessTypeAndExpiresAtBefore(
                eq("MATERIAL_IMPORT_ERROR"),
                any(LocalDateTime.class)))
                .thenReturn(List.of(expiredFile));

        fileObjectService.cleanupExpiredFiles("MATERIAL_IMPORT_ERROR");

        verify(fileStorageService).delete("aps-temp", "materials/error/test.csv");
        verify(fileObjectRepository).delete(expiredFile);
    }
}
