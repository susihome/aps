package com.aps.service.repository;

import com.aps.domain.entity.MaterialMoldBinding;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialMoldBindingRepository extends JpaRepository<MaterialMoldBinding, UUID> {

    @EntityGraph(attributePaths = {"material", "mold"})
    List<MaterialMoldBinding> findAllByOrderByPriorityDescCreateTimeAsc();

    @EntityGraph(attributePaths = {"material", "mold"})
    List<MaterialMoldBinding> findAllByMaterial_IdAndEnabledTrueOrderByIsDefaultDescIsPreferredDescPriorityDescCreateTimeAsc(UUID materialId);

    @EntityGraph(attributePaths = {"material", "mold"})
    List<MaterialMoldBinding> findAllByMold_IdAndEnabledTrueOrderByIsDefaultDescIsPreferredDescPriorityDescCreateTimeAsc(UUID moldId);

    @EntityGraph(attributePaths = {"material", "mold"})
    Optional<MaterialMoldBinding> findById(UUID id);

    boolean existsByMaterial_IdAndMold_Id(UUID materialId, UUID moldId);

    boolean existsByMaterial_Id(UUID materialId);

    boolean existsByMold_Id(UUID moldId);
}
