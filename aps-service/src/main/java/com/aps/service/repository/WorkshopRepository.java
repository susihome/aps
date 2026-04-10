package com.aps.service.repository;

import com.aps.domain.entity.Workshop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, UUID> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"calendar"})
    List<Workshop> findByEnabledTrueOrderBySortOrderAsc();

    @EntityGraph(attributePaths = {"calendar"})
    List<Workshop> findAllByOrderBySortOrderAsc();

    @Override
    @EntityGraph(attributePaths = {"calendar"})
    Optional<Workshop> findById(UUID id);
}
