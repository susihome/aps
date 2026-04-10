package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.CalendarDate;
import com.aps.domain.entity.CalendarShift;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.entity.Resource;
import com.aps.domain.entity.ResourceCapacityDay;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.DateType;
import com.aps.service.exception.ValidationException;
import com.aps.service.repository.CalendarDateRepository;
import com.aps.service.repository.CalendarShiftRepository;
import com.aps.service.repository.ResourceCapacityDayRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceCapacityService {

    private static final BigDecimal DEFAULT_UTILIZATION_RATE = BigDecimal.ONE;

    private final ResourceService resourceService;
    private final CalendarDateRepository calendarDateRepository;
    private final CalendarShiftRepository calendarShiftRepository;
    private final ResourceCapacityDayRepository resourceCapacityDayRepository;

    @Transactional(readOnly = true)
    public List<Resource> getResources() {
        return resourceService.getAllResources(null, null);
    }

    @Transactional(readOnly = true)
    public ResourceCapacityMonthResult getMonthCapacity(UUID resourceId, int year, int month) {
        Resource resource = resourceService.getResourceById(resourceId);
        FactoryCalendar calendar = resourceService.getEffectiveCalendar(resourceId);
        if (calendar == null) {
            throw new ValidationException("设备未绑定有效日历");
        }
        validateMonthRequest(calendar, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<CalendarDate> calendarDates = calendarDateRepository
                .findByCalendarIdAndDateBetweenOrderByDateAsc(calendar.getId(), start, end);
        List<CalendarShift> shifts = calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendar.getId());
        List<ResourceCapacityDay> overrides = resourceCapacityDayRepository
                .findByResourceIdAndCapacityDateBetweenOrderByCapacityDateAsc(resourceId, start, end);

        int baseShiftMinutes = calculateShiftMinutes(shifts);
        Map<LocalDate, CalendarDate> dateMap = toDateMap(calendarDates, start, end);
        Map<LocalDate, ResourceCapacityDay> overrideMap = new HashMap<>();
        for (ResourceCapacityDay override : overrides) {
            overrideMap.put(override.getCapacityDate(), override);
        }

        List<ResourceCapacityDayView> days = yearMonth.atDay(1)
                .datesUntil(end.plusDays(1))
                .map(date -> buildDayView(date, baseShiftMinutes, dateMap.get(date), overrideMap.get(date)))
                .toList();

        long workdayCount = days.stream().filter(day -> day.getDateType() == DateType.WORKDAY).count();
        int totalDefaultShiftMinutes = days.stream().map(ResourceCapacityDayView::getDefaultShiftMinutes).mapToInt(Integer::intValue).sum();
        int totalEffectiveShiftMinutes = days.stream().map(ResourceCapacityDayView::getEffectiveShiftMinutes).mapToInt(Integer::intValue).sum();
        int totalAvailableCapacityMinutes = days.stream().map(ResourceCapacityDayView::getAvailableCapacityMinutes).mapToInt(Integer::intValue).sum();
        BigDecimal averageUtilizationRate = days.isEmpty()
                ? BigDecimal.ZERO
                : days.stream()
                .map(ResourceCapacityDayView::getUtilizationRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(days.size()), 4, RoundingMode.HALF_UP);

        return ResourceCapacityMonthResult.builder()
                .resourceId(resource.getId())
                .resourceCode(resource.getResourceCode())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType())
                .workshopId(resource.getWorkshop() != null ? resource.getWorkshop().getId() : null)
                .workshopName(resource.getWorkshop() != null ? resource.getWorkshop().getName() : null)
                .calendarId(calendar.getId())
                .calendarName(calendar.getName())
                .year(year)
                .month(month)
                .workdayCount(workdayCount)
                .totalDefaultShiftMinutes(totalDefaultShiftMinutes)
                .totalEffectiveShiftMinutes(totalEffectiveShiftMinutes)
                .totalAvailableCapacityMinutes(totalAvailableCapacityMinutes)
                .averageUtilizationRate(averageUtilizationRate)
                .days(days)
                .build();
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "ResourceCapacity")
    public ResourceCapacityDayView updateDay(UUID resourceId, LocalDate date, Integer shiftMinutesOverride,
                                             BigDecimal utilizationRate, String remark) {
        Resource resource = resourceService.getResourceById(resourceId);
        FactoryCalendar calendar = resourceService.getEffectiveCalendar(resourceId);
        if (calendar == null) {
            throw new ValidationException("设备未绑定有效日历");
        }
        validateCapacityDate(calendar, date);

        ResourceCapacityDay day = resourceCapacityDayRepository.findByResourceIdAndCapacityDate(resourceId, date)
                .orElseGet(() -> {
                    ResourceCapacityDay entity = new ResourceCapacityDay();
                    entity.setResource(resource);
                    entity.setCapacityDate(date);
                    return entity;
                });

        day.setShiftMinutesOverride(shiftMinutesOverride);
        day.setUtilizationRate(utilizationRate != null ? utilizationRate : DEFAULT_UTILIZATION_RATE);
        day.setRemark(normalizeRemark(remark));
        ResourceCapacityDay saved = resourceCapacityDayRepository.save(day);

        List<CalendarShift> shifts = calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendar.getId());
        CalendarDate calendarDate = findCalendarDate(calendar, date);
        return buildDayView(date, calculateShiftMinutes(shifts), calendarDate, saved);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "ResourceCapacity")
    public void batchUpdateDays(UUID resourceId, List<LocalDate> dates, Integer shiftMinutesOverride,
                                BigDecimal utilizationRate, String remark) {
        if (dates == null || dates.isEmpty()) {
            throw new ValidationException("日期列表不能为空");
        }
        for (LocalDate date : dates) {
            updateDay(resourceId, date, shiftMinutesOverride, utilizationRate, remark);
        }
    }

    private void validateMonthRequest(FactoryCalendar calendar, int year, int month) {
        if (month < 1 || month > 12) {
            throw new ValidationException("月份必须在1到12之间");
        }
        if (calendar.getYear() == null || calendar.getYear() != year) {
            throw new ValidationException("查询年份与设备有效日历年份不一致");
        }
    }

    private void validateCapacityDate(FactoryCalendar calendar, LocalDate date) {
        if (date == null) {
            throw new ValidationException("日期不能为空");
        }
        if (calendar.getYear() == null || date.getYear() != calendar.getYear()) {
            throw new ValidationException("日期不属于设备有效日历年份");
        }
    }

    private ResourceCapacityDayView buildDayView(LocalDate date, int baseShiftMinutes, CalendarDate calendarDate, ResourceCapacityDay override) {
        DateType dateType = calendarDate != null ? calendarDate.getDateType() : deriveDateType(date);
        String dateLabel = calendarDate != null ? calendarDate.getLabel() : null;
        int defaultShiftMinutes = isWorkday(dateType) ? baseShiftMinutes : 0;
        int effectiveShiftMinutes = override != null && override.getShiftMinutesOverride() != null
                ? override.getShiftMinutesOverride()
                : defaultShiftMinutes;
        BigDecimal utilizationRate = override != null && override.getUtilizationRate() != null
                ? override.getUtilizationRate()
                : DEFAULT_UTILIZATION_RATE;
        int availableCapacityMinutes = BigDecimal.valueOf(effectiveShiftMinutes)
                .multiply(utilizationRate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return ResourceCapacityDayView.builder()
                .date(date)
                .dateType(dateType)
                .dateLabel(dateLabel)
                .defaultShiftMinutes(defaultShiftMinutes)
                .shiftMinutesOverride(override != null ? override.getShiftMinutesOverride() : null)
                .effectiveShiftMinutes(effectiveShiftMinutes)
                .utilizationRate(utilizationRate)
                .availableCapacityMinutes(availableCapacityMinutes)
                .remark(override != null ? override.getRemark() : null)
                .overridden(override != null && override.getShiftMinutesOverride() != null)
                .build();
    }

    private Map<LocalDate, CalendarDate> toDateMap(List<CalendarDate> calendarDates, LocalDate start, LocalDate end) {
        Map<LocalDate, CalendarDate> result = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            result.put(date, null);
        }
        for (CalendarDate calendarDate : calendarDates) {
            result.put(calendarDate.getDate(), calendarDate);
        }
        return result;
    }

    private CalendarDate findCalendarDate(FactoryCalendar calendar, LocalDate date) {
        return calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendar.getId(), date, date)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private int calculateShiftMinutes(List<CalendarShift> shifts) {
        return shifts.stream()
                .mapToInt(this::calculateShiftMinutes)
                .sum();
    }

    private int calculateShiftMinutes(CalendarShift shift) {
        LocalTime start = shift.getStartTime();
        LocalTime end = shift.getEndTime();
        int totalMinutes;
        if (Boolean.TRUE.equals(shift.getNextDay())) {
            totalMinutes = (24 * 60 - start.toSecondOfDay() / 60) + (end.toSecondOfDay() / 60);
        } else {
            totalMinutes = (int) Duration.between(start, end).toMinutes();
        }
        int breakMinutes = shift.getBreakMinutes() != null ? shift.getBreakMinutes() : 0;
        return Math.max(totalMinutes - breakMinutes, 0);
    }

    private DateType deriveDateType(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> DateType.RESTDAY;
            default -> DateType.WORKDAY;
        };
    }

    private boolean isWorkday(DateType dateType) {
        return dateType == DateType.WORKDAY;
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Value
    @Builder
    public static class ResourceCapacityDayView {
        LocalDate date;
        DateType dateType;
        String dateLabel;
        Integer defaultShiftMinutes;
        Integer shiftMinutesOverride;
        Integer effectiveShiftMinutes;
        BigDecimal utilizationRate;
        Integer availableCapacityMinutes;
        String remark;
        boolean overridden;
    }

    @Value
    @Builder
    public static class ResourceCapacityMonthResult {
        UUID resourceId;
        String resourceCode;
        String resourceName;
        String resourceType;
        UUID workshopId;
        String workshopName;
        UUID calendarId;
        String calendarName;
        int year;
        int month;
        long workdayCount;
        int totalDefaultShiftMinutes;
        int totalEffectiveShiftMinutes;
        int totalAvailableCapacityMinutes;
        BigDecimal averageUtilizationRate;
        List<ResourceCapacityDayView> days;
    }
}
