package com.aps.domain.entity;

import com.aps.domain.enums.AuthSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
@Getter
@Setter
public class AuthSession extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private Long sessionVersion;

    @Column(nullable = false, length = 64)
    private String refreshTokenJti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthSessionStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime lastAccessAt;

    @Column(length = 32)
    private String clientType;

    @Column(length = 64)
    private String clientIp;

    @Column(length = 512)
    private String userAgent;
}
