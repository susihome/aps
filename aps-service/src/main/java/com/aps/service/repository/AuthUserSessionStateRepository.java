package com.aps.service.repository;

import com.aps.domain.entity.AuthUserSessionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthUserSessionStateRepository extends JpaRepository<AuthUserSessionState, UUID> {
}
