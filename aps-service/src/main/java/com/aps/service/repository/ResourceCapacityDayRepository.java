package com.aps.service.repository;

import com.aps.domain.entity.ResourceCapacityDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceCapacityDayRepository extends JpaRepository<ResourceCapacityDay, UUID> {

    List<ResourceCapacityDay> findByResourceIdAndCapacityDateBetweenOrderByCapacityDateAsc(UUID resourceId, LocalDate start, LocalDate end);

    Optional<ResourceCapacityDay> findByResourceIdAndCapacityDate(UUID resourceId, LocalDate capacityDate);
}
