package com.aps.domain.entity;

import com.aps.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_timestamp", columnList = "user_id,timestamp"),
    @Index(name = "idx_action_timestamp", columnList = "action,timestamp")
})
@Getter
@Setter
public class AuditLog extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(length = 100)
    private String resource;

    @Column(columnDefinition = "TEXT")
    private String details;  // JSON格式的详细信息

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
