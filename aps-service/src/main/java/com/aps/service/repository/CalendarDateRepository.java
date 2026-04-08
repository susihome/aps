package com.aps.service.repository;

import com.aps.domain.entity.CalendarDate;
import com.aps.domain.enums.DateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarDateRepository extends JpaRepository<CalendarDate, UUID> {
    List<CalendarDate> findByCalendarIdAndDateBetween(UUID calendarId, LocalDate start, LocalDate end);

    @Modifying
    @Query("UPDATE CalendarDate cd SET cd.dateType = :dateType, cd.label = :label WHERE cd.calendar.id = :calendarId AND cd.date = :date")
    int updateDateType(@Param("calendarId") UUID calendarId, @Param("date") LocalDate date,
                       @Param("dateType") DateType dateType, @Param("label") String label);

    void deleteByCalendarId(UUID calendarId);

    @Query("SELECT COUNT(cd) FROM CalendarDate cd WHERE cd.calendar.id = :calendarId AND cd.dateType = :dateType")
    long countByCalendarIdAndDateType(@Param("calendarId") UUID calendarId, @Param("dateType") DateType dateType);
}
