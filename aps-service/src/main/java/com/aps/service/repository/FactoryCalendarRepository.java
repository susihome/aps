package com.aps.service.repository;

import com.aps.domain.entity.FactoryCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FactoryCalendarRepository extends JpaRepository<FactoryCalendar, UUID> {
    boolean existsByCode(String code);
    List<FactoryCalendar> findByYearOrderByCreateTimeDesc(Integer year);
    Optional<FactoryCalendar> findByIsDefaultTrue();
}
