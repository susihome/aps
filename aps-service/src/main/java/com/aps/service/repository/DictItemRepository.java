package com.aps.service.repository;

import com.aps.domain.entity.DictItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DictItemRepository extends JpaRepository<DictItem, UUID> {

    boolean existsByDictTypeIdAndItemCode(UUID dictTypeId, String itemCode);

    boolean existsByDictTypeIdAndItemCodeAndIdNot(UUID dictTypeId, String itemCode, UUID id);

    long countByDictTypeId(UUID dictTypeId);

    @Query("""
            SELECT di FROM DictItem di
            WHERE di.dictType.id = :dictTypeId
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(di.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(di.itemName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(di.itemValue) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:enabled IS NULL OR di.enabled = :enabled)
            """)
    Page<DictItem> searchByTypeId(@Param("dictTypeId") UUID dictTypeId,
                                  @Param("keyword") String keyword,
                                  @Param("enabled") Boolean enabled,
                                  Pageable pageable);

    @Query("""
            SELECT di FROM DictItem di
            WHERE di.dictType.code = :typeCode AND di.enabled = true
            ORDER BY di.sortOrder ASC, di.id ASC
            """)
    List<DictItem> findEnabledItemsByTypeCode(@Param("typeCode") String typeCode);
}
