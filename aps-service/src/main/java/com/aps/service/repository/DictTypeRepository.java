package com.aps.service.repository;

import com.aps.domain.entity.DictType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DictTypeRepository extends JpaRepository<DictType, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<DictType> findByCode(String code);

    @Query("""
            SELECT dt FROM DictType dt
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(dt.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(dt.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:enabled IS NULL OR dt.enabled = :enabled)
            """)
    Page<DictType> search(@Param("keyword") String keyword,
                          @Param("enabled") Boolean enabled,
                          Pageable pageable);
}
