package com.aps.service.repository;

import com.aps.domain.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {

    Optional<Material> findByMaterialCode(String materialCode);

    boolean existsByMaterialCode(String materialCode);

    boolean existsByMaterialCodeAndIdNot(String materialCode, UUID id);

    List<Material> findAllByOrderByMaterialCodeAsc();

    Page<Material> findAllByOrderByMaterialCodeAsc(Pageable pageable);

    List<Material> findByMaterialCodeIn(Collection<String> materialCodes);

    List<Material> findByMaterialCodeContainingIgnoreCaseOrMaterialNameContainingIgnoreCaseOrSpecificationContainingIgnoreCaseOrderByMaterialCodeAsc(
            String materialCode, String materialName, String specification, Pageable pageable);
}
