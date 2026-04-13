package com.aps.service.repository;

import com.aps.domain.entity.AuthSession;
import com.aps.domain.enums.AuthSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByIdAndUserId(UUID id, UUID userId);

    List<AuthSession> findByUserIdAndStatus(UUID userId, AuthSessionStatus status);

    List<AuthSession> findTop100ByStatusAndExpiresAtBefore(AuthSessionStatus status, LocalDateTime expiresAt);
}
