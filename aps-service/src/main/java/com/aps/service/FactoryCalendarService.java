package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.CalendarDate;
import com.aps.domain.entity.CalendarShift;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.DateType;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.CalendarDateRepository;
import com.aps.service.repository.CalendarShiftRepository;
import com.aps.service.repository.FactoryCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryCalendarService {

    private final FactoryCalendarRepository calendarRepository;
    private final CalendarShiftRepository shiftRepository;
    private final CalendarDateRepository dateRepository;

    // ========== 日历CRUD ==========

    @Transactional(readOnly = true)
    public List<FactoryCalendar> getAllCalendars() {
        return calendarRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FactoryCalendar> getCalendarsByYear(Integer year) {
        return calendarRepository.findByYearOrderByCreateTimeDesc(year);
    }

    @Transactional(readOnly = true)
    public FactoryCalendar getCalendarById(UUID id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("日历不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, resource = "FactoryCalendar")
    public FactoryCalendar createCalendar(String name, String code, Integer year, String description) {
        if (calendarRepository.existsByCode(code)) {
            throw new ResourceConflictException("日历编码已存在: " + code);
        }
        FactoryCalendar calendar = new FactoryCalendar();
        calendar.setName(name);
        calendar.setCode(code);
        calendar.setYear(year);
        calendar.setDescription(description);
        calendar.setIsDefault(false);
        calendar.setEnabled(true);

        FactoryCalendar saved = calendarRepository.save(calendar);
        initCalendarDates(saved);
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public FactoryCalendar updateCalendar(UUID id, String name, String description, Boolean enabled) {
        FactoryCalendar calendar = getCalendarById(id);
        if (name != null) calendar.setName(name);
        if (description != null) calendar.setDescription(description);
        if (enabled != null) calendar.setEnabled(enabled);
        return calendarRepository.save(calendar);
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, resource = "FactoryCalendar")
    public void deleteCalendar(UUID id) {
        if (!calendarRepository.existsById(id)) {
            throw new ResourceNotFoundException("日历不存在: " + id);
        }
        calendarRepository.deleteById(id);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void setDefaultCalendar(UUID id) {
        // 取消旧的默认
        calendarRepository.findByIsDefaultTrue().ifPresent(old -> {
            old.setIsDefault(false);
            calendarRepository.save(old);
        });
        FactoryCalendar calendar = getCalendarById(id);
        calendar.setIsDefault(true);
        calendarRepository.save(calendar);
    }

    // ========== 班次管理 ==========

    @Transactional(readOnly = true)
    public List<CalendarShift> getShifts(UUID calendarId) {
        return shiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public CalendarShift addShift(UUID calendarId, String name, LocalTime startTime, LocalTime endTime, Integer sortOrder, Boolean nextDay) {
        // 验证班次时间
        validateShiftTime(startTime, endTime, nextDay);

        FactoryCalendar calendar = getCalendarById(calendarId);
        CalendarShift shift = new CalendarShift();
        shift.setCalendar(calendar);
        shift.setName(name);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setSortOrder(sortOrder != null ? sortOrder : 0);
        shift.setNextDay(nextDay != null ? nextDay : false);
        return shiftRepository.save(shift);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public CalendarShift updateShift(UUID shiftId, String name, LocalTime startTime, LocalTime endTime, Integer sortOrder, Boolean nextDay) {
        CalendarShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("班次不存在: " + shiftId));

        // 验证班次时间（如果提供了新时间）
        LocalTime newStartTime = startTime != null ? startTime : shift.getStartTime();
        LocalTime newEndTime = endTime != null ? endTime : shift.getEndTime();
        Boolean newNextDay = nextDay != null ? nextDay : shift.getNextDay();
        validateShiftTime(newStartTime, newEndTime, newNextDay);

        if (name != null) shift.setName(name);
        if (startTime != null) shift.setStartTime(startTime);
        if (endTime != null) shift.setEndTime(endTime);
        if (sortOrder != null) shift.setSortOrder(sortOrder);
        if (nextDay != null) shift.setNextDay(nextDay);
        return shiftRepository.save(shift);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void deleteShift(UUID shiftId) {
        if (!shiftRepository.existsById(shiftId)) {
            throw new ResourceNotFoundException("班次不存在: " + shiftId);
        }
        shiftRepository.deleteById(shiftId);
    }

    // ========== 日期管理 ==========

    @Transactional(readOnly = true)
    public List<CalendarDate> getDatesByMonth(UUID calendarId, Integer year, Integer month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, start, end);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void updateDateType(UUID calendarId, LocalDate date, DateType dateType, String label) {
        List<CalendarDate> existing = dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, date, date);
        if (existing.isEmpty()) {
            FactoryCalendar calendar = getCalendarById(calendarId);
            CalendarDate cd = new CalendarDate();
            cd.setCalendar(calendar);
            cd.setDate(date);
            cd.setDateType(dateType);
            cd.setLabel(label);
            dateRepository.save(cd);
        } else {
            dateRepository.updateDateType(calendarId, date, dateType, label);
        }
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void batchUpdateDates(UUID calendarId, List<LocalDate> dates, DateType dateType, String label) {
        if (dates == null || dates.isEmpty()) {
            return;
        }
        // 使用批量更新，避免 N+1 查询问题
        dateRepository.batchUpdateDateTypes(calendarId, dates, dateType, label);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void batchSetHolidays(UUID calendarId, List<LocalDate> dates, String label) {
        batchUpdateDates(calendarId, dates, DateType.HOLIDAY, label);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "FactoryCalendar")
    public void applyWeekendPattern(UUID calendarId, String pattern) {
        String normalizedPattern = pattern == null ? "" : pattern.trim().toUpperCase();
        if (normalizedPattern.isEmpty()) {
            throw new ResourceConflictException("单双休模式不能为空");
        }

        FactoryCalendar calendar = getCalendarById(calendarId);
        LocalDate start = LocalDate.of(calendar.getYear(), 1, 1);
        LocalDate end = LocalDate.of(calendar.getYear(), 12, 31);
        List<CalendarDate> dates = dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, start, end);

        for (CalendarDate calendarDate : dates) {
            if (calendarDate.getDateType() == DateType.HOLIDAY) {
                continue;
            }

            DateType targetDateType = isRestDayByPattern(calendarDate.getDate().getDayOfWeek(), normalizedPattern)
                    ? DateType.RESTDAY
                    : DateType.WORKDAY;

            if (calendarDate.getDateType() != targetDateType || calendarDate.getLabel() != null) {
                dateRepository.updateDateType(calendarId, calendarDate.getDate(), targetDateType, null);
            }
        }
    }

    // ========== 统计 ==========

    @Transactional(readOnly = true)
    public long countWorkdays(UUID calendarId, Integer year, Integer month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<CalendarDate> dates = dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, start, end);
        return dates.stream().filter(d -> d.getDateType() == DateType.WORKDAY).count();
    }

    @Transactional(readOnly = true)
    public long countYearWorkdays(UUID calendarId, Integer year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<CalendarDate> dates = dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, start, end);
        return dates.stream().filter(d -> d.getDateType() == DateType.WORKDAY).count();
    }

    // ========== 私有方法 ==========

    /**
     * 验证班次时间设置
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param nextDay 是否跨天
     * @throws IllegalArgumentException 时间设置不合法时抛出
     */
    private void validateShiftTime(LocalTime startTime, LocalTime endTime, Boolean nextDay) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }

        boolean isNextDay = nextDay != null && nextDay;

        if (!isNextDay) {
            // 非跨天班次：结束时间必须大于开始时间
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new IllegalArgumentException(
                    String.format("非跨天班次的结束时间(%s)必须大于开始时间(%s)", endTime, startTime)
                );
            }
        } else {
            // 跨天班次：结束时间应该小于或等于开始时间（表示次日）
            // 允许相等的情况（如 00:00-00:00，表示24小时班次）
            if (endTime.isAfter(startTime)) {
                throw new IllegalArgumentException(
                    String.format("跨天班次的结束时间(%s)应该小于或等于开始时间(%s)", endTime, startTime)
                );
            }
        }
    }

    private void initCalendarDates(FactoryCalendar calendar) {
        LocalDate start = LocalDate.of(calendar.getYear(), 1, 1);
        LocalDate end = LocalDate.of(calendar.getYear(), 12, 31);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            CalendarDate cd = new CalendarDate();
            cd.setCalendar(calendar);
            cd.setDate(date);
            // 周六周日为休息日，其余为工作日
            cd.setDateType(date.getDayOfWeek().getValue() >= 6 ? DateType.RESTDAY : DateType.WORKDAY);
            calendar.getDates().add(cd);
        }
    }

    private boolean isRestDayByPattern(DayOfWeek dayOfWeek, String pattern) {
        if ("SINGLE".equalsIgnoreCase(pattern)) {
            return dayOfWeek == DayOfWeek.SUNDAY;
        }
        if ("DOUBLE".equalsIgnoreCase(pattern)) {
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }
        throw new ResourceConflictException("不支持的单双休模式: " + pattern);
    }
}
