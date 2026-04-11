package com.aps.service.repository;

import com.aps.domain.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    boolean existsByRequiredMaterial_Id(UUID materialId);

    boolean existsByRequiredMold_Id(UUID moldId);
}
