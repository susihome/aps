package com.aps.service.repository;

import com.aps.domain.entity.ScheduleTimeParameter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleTimeParameterRepository extends JpaRepository<ScheduleTimeParameter, UUID> {

    Optional<ScheduleTimeParameter> findByResource_Id(UUID resourceId);

    Optional<ScheduleTimeParameter> findByResourceIsNull();

    Optional<ScheduleTimeParameter> findByIsDefaultTrue();

    Optional<ScheduleTimeParameter> findByIsDefaultTrueAndEnabledTrue();

    Optional<ScheduleTimeParameter> findByResource_IdAndEnabledTrue(UUID resourceId);

    List<ScheduleTimeParameter> findAllByEnabledTrue();

    @Override
    @EntityGraph(attributePaths = {"resource"})
    List<ScheduleTimeParameter> findAll();

    @Override
    @EntityGraph(attributePaths = {"resource"})
    Optional<ScheduleTimeParameter> findById(UUID id);

    boolean existsByResource_Id(UUID resourceId);

    boolean existsByIsDefaultTrue();
}
