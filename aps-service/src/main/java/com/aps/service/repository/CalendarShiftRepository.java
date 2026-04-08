package com.aps.service.repository;

import com.aps.domain.entity.CalendarShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarShiftRepository extends JpaRepository<CalendarShift, UUID> {
    List<CalendarShift> findByCalendarIdOrderBySortOrderAsc(UUID calendarId);
    void deleteByCalendarId(UUID calendarId);
}
