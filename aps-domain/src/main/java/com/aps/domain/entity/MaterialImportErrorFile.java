package com.aps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "material_import_error_files")
@Getter
@Setter
public class MaterialImportErrorFile extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String fileName;

    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] content;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
