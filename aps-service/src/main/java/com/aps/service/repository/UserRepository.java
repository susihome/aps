package com.aps.service.repository;

import com.aps.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id IN :userIds")
    List<User> findAllWithRolesByIdIn(@Param("userIds") List<UUID> userIds);

    @Query(value = """
            SELECT u FROM User u
            WHERE (:keyword = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:enabled IS NULL OR u.enabled = :enabled)
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:keyword = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:enabled IS NULL OR u.enabled = :enabled)
            """)
    org.springframework.data.domain.Page<User> search(
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            org.springframework.data.domain.Pageable pageable);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
}
