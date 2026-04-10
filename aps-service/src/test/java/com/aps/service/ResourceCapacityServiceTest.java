package com.aps.service;

import com.aps.domain.entity.CalendarDate;
import com.aps.domain.entity.CalendarShift;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.entity.Resource;
import com.aps.domain.entity.ResourceCapacityDay;
import com.aps.domain.entity.Workshop;
import com.aps.domain.enums.DateType;
import com.aps.service.exception.ValidationException;
import com.aps.service.repository.CalendarDateRepository;
import com.aps.service.repository.CalendarShiftRepository;
import com.aps.service.repository.ResourceCapacityDayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("设备日产能服务测试")
class ResourceCapacityServiceTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private CalendarDateRepository calendarDateRepository;

    @Mock
    private CalendarShiftRepository calendarShiftRepository;

    @Mock
    private ResourceCapacityDayRepository resourceCapacityDayRepository;

    @InjectMocks
    private ResourceCapacityService resourceCapacityService;

    private UUID resourceId;
    private UUID calendarId;
    private Resource resource;
    private FactoryCalendar calendar;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID();
        calendarId = UUID.randomUUID();

        Workshop workshop = new Workshop();
        workshop.setId(UUID.randomUUID());
        workshop.setName("注塑车间");

        resource = new Resource();
        resource.setId(resourceId);
        resource.setResourceCode("M-001");
        resource.setResourceName("1号机");
        resource.setResourceType("INJECTION_MACHINE");
        resource.setWorkshop(workshop);

        calendar = new FactoryCalendar();
        calendar.setId(calendarId);
        calendar.setName("默认日历");
        calendar.setYear(2026);
    }

    @Nested
    @DisplayName("月度查询")
    class GetMonthCapacityTests {

        @Test
        @DisplayName("工作日和休息日应正确计算产能")
        void getMonthCapacity_shouldBuildMonthView() {
            LocalDate workday = LocalDate.of(2026, 4, 1);
            LocalDate restday = LocalDate.of(2026, 4, 2);

            CalendarDate day1 = new CalendarDate();
            day1.setDate(workday);
            day1.setDateType(DateType.WORKDAY);

            CalendarDate day2 = new CalendarDate();
            day2.setDate(restday);
            day2.setDateType(DateType.RESTDAY);
            day2.setLabel("清明调休");

            CalendarShift shift = new CalendarShift();
            shift.setStartTime(LocalTime.of(8, 0));
            shift.setEndTime(LocalTime.of(16, 0));
            shift.setBreakMinutes(30);
            shift.setNextDay(false);

            ResourceCapacityDay override = new ResourceCapacityDay();
            override.setCapacityDate(restday);
            override.setShiftMinutesOverride(120);
            override.setUtilizationRate(new BigDecimal("0.50"));
            override.setRemark("临时开机");

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);
            when(calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(eq(calendarId), any(), any()))
                    .thenReturn(List.of(day1, day2));
            when(calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId)).thenReturn(List.of(shift));
            when(resourceCapacityDayRepository.findByResourceIdAndCapacityDateBetweenOrderByCapacityDateAsc(eq(resourceId), any(), any()))
                    .thenReturn(List.of(override));

            ResourceCapacityService.ResourceCapacityMonthResult result = resourceCapacityService.getMonthCapacity(resourceId, 2026, 4);

            assertThat(result.getResourceId()).isEqualTo(resourceId);
            assertThat(result.getCalendarId()).isEqualTo(calendarId);
            assertThat(result.getDays()).hasSize(30);
            assertThat(result.getDays().get(0).getDefaultShiftMinutes()).isEqualTo(450);
            assertThat(result.getDays().get(0).getAvailableCapacityMinutes()).isEqualTo(450);
            assertThat(result.getDays().get(1).getDateType()).isEqualTo(DateType.RESTDAY);
            assertThat(result.getDays().get(1).getDefaultShiftMinutes()).isZero();
            assertThat(result.getDays().get(1).getEffectiveShiftMinutes()).isEqualTo(120);
            assertThat(result.getDays().get(1).getAvailableCapacityMinutes()).isEqualTo(60);
            assertThat(result.getDays().get(1).getRemark()).isEqualTo("临时开机");
        }

        @Test
        @DisplayName("跨天班次应累计到默认班次分钟数")
        void getMonthCapacity_shouldSupportNextDayShift() {
            LocalDate date = LocalDate.of(2026, 4, 1);
            CalendarDate workday = new CalendarDate();
            workday.setDate(date);
            workday.setDateType(DateType.WORKDAY);

            CalendarShift shift = new CalendarShift();
            shift.setStartTime(LocalTime.of(20, 0));
            shift.setEndTime(LocalTime.of(8, 0));
            shift.setBreakMinutes(60);
            shift.setNextDay(true);

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);
            when(calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(eq(calendarId), any(), any()))
                    .thenReturn(List.of(workday));
            when(calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId)).thenReturn(List.of(shift));
            when(resourceCapacityDayRepository.findByResourceIdAndCapacityDateBetweenOrderByCapacityDateAsc(eq(resourceId), any(), any()))
                    .thenReturn(List.of());

            ResourceCapacityService.ResourceCapacityMonthResult result = resourceCapacityService.getMonthCapacity(resourceId, 2026, 4);

            assertThat(result.getDays().get(0).getDefaultShiftMinutes()).isEqualTo(660);
            assertThat(result.getDays().get(0).getAvailableCapacityMinutes()).isEqualTo(660);
        }

        @Test
        @DisplayName("查询月份与日历年份不一致时应抛出异常")
        void getMonthCapacity_withCalendarYearMismatch_shouldThrow() {
            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            calendar.setYear(2025);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);

            assertThatThrownBy(() -> resourceCapacityService.getMonthCapacity(resourceId, 2026, 4))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("日历年份不一致");
        }

        @Test
        @DisplayName("缺少有效日历时应抛出异常")
        void getMonthCapacity_withoutEffectiveCalendar_shouldThrow() {
            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(null);

            assertThatThrownBy(() -> resourceCapacityService.getMonthCapacity(resourceId, 2026, 4))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("有效日历");
        }
    }

    @Nested
    @DisplayName("单日更新")
    class UpdateDayTests {

        @Test
        @DisplayName("更新单日应保存覆盖数据并返回计算结果")
        void updateDay_shouldPersistOverride() {
            LocalDate date = LocalDate.of(2026, 4, 3);

            CalendarDate workday = new CalendarDate();
            workday.setDate(date);
            workday.setDateType(DateType.WORKDAY);

            CalendarShift shift = new CalendarShift();
            shift.setStartTime(LocalTime.of(8, 0));
            shift.setEndTime(LocalTime.of(16, 0));
            shift.setBreakMinutes(60);
            shift.setNextDay(false);

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);
            when(resourceCapacityDayRepository.findByResourceIdAndCapacityDate(resourceId, date)).thenReturn(Optional.empty());
            when(resourceCapacityDayRepository.save(any(ResourceCapacityDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId)).thenReturn(List.of(shift));
            when(calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, date, date)).thenReturn(List.of(workday));

            ResourceCapacityService.ResourceCapacityDayView result = resourceCapacityService.updateDay(
                    resourceId,
                    date,
                    300,
                    new BigDecimal("0.80"),
                    "加班"
            );

            assertThat(result.getShiftMinutesOverride()).isEqualTo(300);
            assertThat(result.getEffectiveShiftMinutes()).isEqualTo(300);
            assertThat(result.getAvailableCapacityMinutes()).isEqualTo(240);
            assertThat(result.getRemark()).isEqualTo("加班");
            verify(resourceCapacityDayRepository).save(any(ResourceCapacityDay.class));
        }

        @Test
        @DisplayName("更新跨年日期应抛出异常")
        void updateDay_withDateOutOfCalendarYear_shouldThrow() {
            LocalDate date = LocalDate.of(2025, 4, 3);
            calendar.setYear(2026);

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);

            assertThatThrownBy(() -> resourceCapacityService.updateDay(resourceId, date, 300, new BigDecimal("0.80"), "加班"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("有效日历年份");
        }

        @Test
        @DisplayName("空备注应标准化为 null")
        void updateDay_shouldNormalizeBlankRemark() {
            LocalDate date = LocalDate.of(2026, 4, 3);
            CalendarShift shift = new CalendarShift();
            shift.setStartTime(LocalTime.of(8, 0));
            shift.setEndTime(LocalTime.of(16, 0));
            shift.setBreakMinutes(60);
            shift.setNextDay(false);

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);
            when(resourceCapacityDayRepository.findByResourceIdAndCapacityDate(resourceId, date)).thenReturn(Optional.empty());
            when(resourceCapacityDayRepository.save(any(ResourceCapacityDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId)).thenReturn(List.of(shift));
            when(calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(calendarId, date, date)).thenReturn(List.of());

            ResourceCapacityService.ResourceCapacityDayView result = resourceCapacityService.updateDay(
                    resourceId,
                    date,
                    null,
                    new BigDecimal("1.00"),
                    "   "
            );

            assertThat(result.getRemark()).isNull();
            assertThat(result.getEffectiveShiftMinutes()).isEqualTo(420);
        }
    }

    @Nested
    @DisplayName("批量更新")
    class BatchUpdateTests {

        @Test
        @DisplayName("空日期列表应抛出校验异常")
        void batchUpdateDays_withEmptyDates_shouldThrow() {
            assertThatThrownBy(() -> resourceCapacityService.batchUpdateDays(resourceId, List.of(), 60, BigDecimal.ONE, null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("日期列表不能为空");

            verify(resourceCapacityDayRepository, never()).save(any());
        }

        @Test
        @DisplayName("批量更新应逐日保存")
        void batchUpdateDays_shouldUpdateEachDate() {
            LocalDate date1 = LocalDate.of(2026, 4, 1);
            LocalDate date2 = LocalDate.of(2026, 4, 2);
            CalendarShift shift = new CalendarShift();
            shift.setStartTime(LocalTime.of(8, 0));
            shift.setEndTime(LocalTime.of(16, 0));
            shift.setBreakMinutes(30);
            shift.setNextDay(false);

            when(resourceService.getResourceById(resourceId)).thenReturn(resource);
            when(resourceService.getEffectiveCalendar(resourceId)).thenReturn(calendar);
            when(resourceCapacityDayRepository.findByResourceIdAndCapacityDate(eq(resourceId), any())).thenReturn(Optional.empty());
            when(resourceCapacityDayRepository.save(any(ResourceCapacityDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(calendarShiftRepository.findByCalendarIdOrderBySortOrderAsc(calendarId)).thenReturn(List.of(shift));
            when(calendarDateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(eq(calendarId), any(), any())).thenReturn(List.of());

            resourceCapacityService.batchUpdateDays(resourceId, List.of(date1, date2), 120, new BigDecimal("0.50"), "批量");

            verify(resourceCapacityDayRepository).findByResourceIdAndCapacityDate(resourceId, date1);
            verify(resourceCapacityDayRepository).findByResourceIdAndCapacityDate(resourceId, date2);
            verify(resourceCapacityDayRepository, org.mockito.Mockito.times(2)).save(any(ResourceCapacityDay.class));
        }
    }
}
