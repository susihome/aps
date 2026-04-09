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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
                        calendarService.countYearWorkdays(cal.getId(), cal.getYear())))
                .toList();
        return AjaxResult.success(dtos);
    }

    @GetMapping("/{id}")
    public AjaxResult<FactoryCalendarDto> getCalendar(@PathVariable UUID id) {
        FactoryCalendar cal = calendarService.getCalendarById(id);
        return AjaxResult.success(FactoryCalendarDto.fromEntity(cal));
    }

    @PostMapping
    public AjaxResult<FactoryCalendarDto> createCalendar(@Valid @RequestBody CreateCalendarRequest request) {
        FactoryCalendar cal = calendarService.createCalendar(
                request.name(), request.code(), request.year(), request.description());
        return AjaxResult.success(FactoryCalendarDto.fromEntity(cal));
    }

    @PutMapping("/{id}")
    public AjaxResult<FactoryCalendarDto> updateCalendar(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateCalendarRequest request) {
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
                                                  @Valid @RequestBody ShiftRequest request) {
        CalendarShift shift = calendarService.addShift(
                id, request.name(), request.startTime(), request.endTime(), request.sortOrder(), request.nextDay());
        return AjaxResult.success(CalendarShiftDto.fromEntity(shift));
    }

    @PutMapping("/{id}/shifts/{shiftId}")
    public AjaxResult<CalendarShiftDto> updateShift(@PathVariable UUID id,
                                                     @PathVariable UUID shiftId,
                                                     @Valid @RequestBody ShiftRequest request) {
        CalendarShift shift = calendarService.updateShift(
                shiftId, request.name(), request.startTime(), request.endTime(), request.sortOrder(), request.nextDay());
        return AjaxResult.success(CalendarShiftDto.fromEntity(shift));
    }

    @DeleteMapping("/{id}/shifts/{shiftId}")
    public AjaxResult<Void> deleteShift(@PathVariable UUID id,
                                         @PathVariable UUID shiftId) {
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
                                            @Valid @RequestBody UpdateDateRequest request) {
        calendarService.updateDateType(id, request.date(), request.dateType(), request.label());
        return AjaxResult.success(null);
    }

    @PostMapping("/{id}/dates/holidays")
    public AjaxResult<Void> batchSetHolidays(@PathVariable UUID id,
                                              @Valid @RequestBody BatchDateRequest request) {
        calendarService.batchSetHolidays(id, request.dates(), request.label());
        return AjaxResult.success(null);
    }

    @PutMapping("/{id}/dates/batch")
    public AjaxResult<Void> batchUpdateDates(@PathVariable UUID id,
                                               @Valid @RequestBody BatchUpdateDateRequest request) {
        calendarService.batchUpdateDates(id, request.dates(), request.dateType(), request.label());
        return AjaxResult.success(null);
    }

    @PutMapping("/{id}/dates/weekend-pattern")
    public AjaxResult<Void> applyWeekendPattern(@PathVariable UUID id,
                                                @Valid @RequestBody WeekendPatternRequest request) {
        calendarService.applyWeekendPattern(id, request.pattern());
        return AjaxResult.success(null);
    }

    // ===== Request Records =====
    public record CreateCalendarRequest(
            @NotBlank(message = "日历名称不能为空")
            String name,
            @NotBlank(message = "日历编码不能为空")
            String code,
            @NotNull(message = "年份不能为空")
            @Min(value = 2020, message = "年份不能早于2020年")
            @Max(value = 2099, message = "年份不能晚于2099年")
            Integer year,
            String description) {}

    public record UpdateCalendarRequest(
            @NotBlank(message = "日历名称不能为空")
            String name,
            String description,
            Boolean enabled) {}

    public record ShiftRequest(
            @NotBlank(message = "班次名称不能为空")
            String name,
            @NotNull(message = "开始时间不能为空")
            LocalTime startTime,
            @NotNull(message = "结束时间不能为空")
            LocalTime endTime,
            @Min(value = 0, message = "排序值不能为负数")
            Integer sortOrder,
            @NotNull(message = "是否跨天不能为空")
            Boolean nextDay) {}

    public record UpdateDateRequest(
            @NotNull(message = "日期不能为空")
            LocalDate date,
            @NotNull(message = "日期类型不能为空")
            DateType dateType,
            String label) {}

    public record BatchDateRequest(
            @NotNull(message = "日期列表不能为空")
            List<LocalDate> dates,
            String label) {}

    public record BatchUpdateDateRequest(
            @NotNull(message = "日期列表不能为空")
            List<LocalDate> dates,
            @NotNull(message = "日期类型不能为空")
            DateType dateType,
            String label) {}

    public record WeekendPatternRequest(
            @NotBlank(message = "单双休模式不能为空")
            String pattern) {}
}
