package com.aps.service.repository;

import com.aps.domain.entity.MaterialImportErrorFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MaterialImportErrorFileRepository extends JpaRepository<MaterialImportErrorFile, UUID> {

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
