package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.CalendarDateDto;
import com.aps.api.dto.CalendarShiftDto;
import com.aps.api.dto.FactoryCalendarDto;
import com.aps.domain.entity.CalendarDate;
import com.aps.domain.entity.CalendarShift;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.enums.DateType;
import com.aps.service.FactoryCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/factory-calendars")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FactoryCalendarController {

    private final FactoryCalendarService calendarService;

    @GetMapping
    public AjaxResult<List<FactoryCalendarDto>> getAllCalendars(
            @RequestParam(required = false) Integer year
    ) {
        List<FactoryCalendar> calendars = year != null
                ? calendarService.getCalendarsByYear(year)
                : calendarService.getAllCalendars();
        List<FactoryCalendarDto> dtos = calendars.stream()
                .map(cal -> FactoryCalendarDto.fromEntity(cal,
                        calendarService.countWorkdays(cal.getId(), cal.getYear(), 1)))
                .toList();
        return AjaxResult.success(dtos);
    }

    @GetMapping("/{id}")
    public AjaxResult<FactoryCalendarDto> getCalendar(@PathVariable UUID id) {
        FactoryCalendar cal = calendarService.getCalendarById(id);
        return AjaxResult.success(FactoryCalendarDto.fromEntity(cal));
    }

    @PostMapping
    public AjaxResult<FactoryCalendarDto> createCalendar(@RequestBody CreateCalendarRequest request) {
        FactoryCalendar cal = calendarService.createCalendar(
                request.name(), request.code(), request.year(), request.description());
        return AjaxResult.success(FactoryCalendarDto.fromEntity(cal));
    }

    @PutMapping("/{id}")
    public AjaxResult<FactoryCalendarDto> updateCalendar(@PathVariable UUID id,
                                                          @RequestBody UpdateCalendarRequest request) {
        FactoryCalendar cal = calendarService.updateCalendar(
                id, request.name(), request.description(), request.enabled());
        return AjaxResult.success(FactoryCalendarDto.fromEntity(cal));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteCalendar(@PathVariable UUID id) {
        calendarService.deleteCalendar(id);
        return AjaxResult.success(null);
    }

    @PutMapping("/{id}/default")
    public AjaxResult<Void> setDefault(@PathVariable UUID id) {
        calendarService.setDefaultCalendar(id);
        return AjaxResult.success(null);
    }

    // ===== 班次 =====

    @GetMapping("/{id}/shifts")
    public AjaxResult<List<CalendarShiftDto>> getShifts(@PathVariable UUID id) {
        List<CalendarShift> shifts = calendarService.getShifts(id);
        return AjaxResult.success(shifts.stream().map(CalendarShiftDto::fromEntity).toList());
    }

    @PostMapping("/{id}/shifts")
    public AjaxResult<CalendarShiftDto> addShift(@PathVariable UUID id,
                                                  @RequestBody ShiftRequest request) {
        CalendarShift shift = calendarService.addShift(
                id, request.name(), request.startTime(), request.endTime(), request.sortOrder());
        return AjaxResult.success(CalendarShiftDto.fromEntity(shift));
    }

    @PutMapping("/{id}/shifts/{shiftId}")
    public AjaxResult<CalendarShiftDto> updateShift(@PathVariable UUID shiftId,
                                                     @RequestBody ShiftRequest request) {
        CalendarShift shift = calendarService.updateShift(
                shiftId, request.name(), request.startTime(), request.endTime(), request.sortOrder());
        return AjaxResult.success(CalendarShiftDto.fromEntity(shift));
    }

    @DeleteMapping("/{id}/shifts/{shiftId}")
    public AjaxResult<Void> deleteShift(@PathVariable UUID shiftId) {
        calendarService.deleteShift(shiftId);
        return AjaxResult.success(null);
    }

    // ===== 日期 =====

    @GetMapping("/{id}/dates")
    public AjaxResult<List<CalendarDateDto>> getDatesByMonth(
            @PathVariable UUID id,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        List<CalendarDate> dates = calendarService.getDatesByMonth(id, year, month);
        return AjaxResult.success(dates.stream().map(CalendarDateDto::fromEntity).toList());
    }

    @PutMapping("/{id}/dates")
    public AjaxResult<Void> updateDateType(@PathVariable UUID id,
                                            @RequestBody UpdateDateRequest request) {
        calendarService.updateDateType(id, request.date(), request.dateType(), request.label());
        return AjaxResult.success(null);
    }

    @PostMapping("/{id}/dates/holidays")
    public AjaxResult<Void> batchSetHolidays(@PathVariable UUID id,
                                              @RequestBody BatchDateRequest request) {
        calendarService.batchSetHolidays(id, request.dates(), request.label());
        return AjaxResult.success(null);
    }

    @PutMapping("/{id}/dates/batch")
    public AjaxResult<Void> batchUpdateDates(@PathVariable UUID id,
                                               @RequestBody BatchUpdateDateRequest request) {
        calendarService.batchUpdateDates(id, request.dates(), request.dateType(), request.label());
        return AjaxResult.success(null);
    }

    // ===== Request Records =====
    public record CreateCalendarRequest(String name, String code, Integer year, String description) {}
    public record UpdateCalendarRequest(String name, String description, Boolean enabled) {}
    public record ShiftRequest(String name, LocalTime startTime, LocalTime endTime, Integer sortOrder) {}
    public record UpdateDateRequest(LocalDate date, DateType dateType, String label) {}
    public record BatchDateRequest(List<LocalDate> dates, String label) {}
    public record BatchUpdateDateRequest(List<LocalDate> dates, DateType dateType, String label) {}
}
