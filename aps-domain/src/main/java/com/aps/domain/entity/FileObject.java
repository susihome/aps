package com.aps.domain.entity;

import com.aps.domain.enums.FileObjectStatus;
import com.aps.domain.enums.FileVisibility;
import com.aps.domain.enums.StorageProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_file_object")
@Getter
@Setter
public class FileObject extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String businessType;

    @Column(nullable = false, length = 128)
    private String bucketName;

    @Column(nullable = false, length = 512)
    private String objectKey;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(length = 64)
    private String sha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StorageProvider storageProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FileVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FileObjectStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
