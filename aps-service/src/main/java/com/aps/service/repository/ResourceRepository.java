package com.aps.service.repository;

import com.aps.domain.entity.Resource;
import com.aps.domain.enums.MachineStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    @EntityGraph(attributePaths = {"workshop", "calendar"})
    List<Resource> findByAvailableTrue();

    @EntityGraph(attributePaths = {"workshop", "calendar"})
    List<Resource> findByWorkshopId(UUID workshopId);

    @EntityGraph(attributePaths = {"workshop", "calendar"})
    List<Resource> findByWorkshopIdAndStatus(UUID workshopId, MachineStatus status);

    @EntityGraph(attributePaths = {"workshop", "calendar"})
    List<Resource> findByStatus(MachineStatus status);

    Optional<Resource> findByResourceCode(String resourceCode);

    boolean existsByWorkshopId(UUID workshopId);

    @Override
    @EntityGraph(attributePaths = {"workshop", "calendar"})
    List<Resource> findAll();

    @Override
    @EntityGraph(attributePaths = {"workshop", "calendar"})
    Optional<Resource> findById(UUID id);
}
