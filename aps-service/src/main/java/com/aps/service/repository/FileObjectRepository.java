package com.aps.service.repository;

import com.aps.domain.entity.FileObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FileObjectRepository extends JpaRepository<FileObject, UUID> {

    List<FileObject> findTop100ByBusinessTypeAndExpiresAtBefore(String businessType, LocalDateTime expiresAt);
}
