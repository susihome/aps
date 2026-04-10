package com.aps.service;

import com.aps.domain.entity.CalendarDate;
import com.aps.domain.entity.CalendarShift;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.enums.DateType;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.CalendarDateRepository;
import com.aps.service.repository.CalendarShiftRepository;
import com.aps.service.repository.FactoryCalendarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("工厂日历服务测试")
class FactoryCalendarServiceTest {

    @Mock
    private FactoryCalendarRepository calendarRepository;

    @Mock
    private CalendarShiftRepository shiftRepository;

    @Mock
    private CalendarDateRepository dateRepository;

    @InjectMocks
    private FactoryCalendarService calendarService;

    private FactoryCalendar testCalendar;
    private UUID calendarId;
    private UUID shiftId;
    private FactoryCalendar anotherCalendar;
    private UUID anotherCalendarId;

    @BeforeEach
    void setUp() {
        calendarId = UUID.randomUUID();
        shiftId = UUID.randomUUID();

        testCalendar = new FactoryCalendar();
        testCalendar.setId(calendarId);
        testCalendar.setName("测试日历");
        testCalendar.setCode("TEST-2024");
        testCalendar.setYear(2024);
        testCalendar.setIsDefault(false);
        testCalendar.setEnabled(true);

        anotherCalendarId = UUID.randomUUID();
        anotherCalendar = new FactoryCalendar();
        anotherCalendar.setId(anotherCalendarId);
        anotherCalendar.setName("另一个日历");
        anotherCalendar.setCode("OTHER-2024");
        anotherCalendar.setYear(2024);
        anotherCalendar.setIsDefault(false);
        anotherCalendar.setEnabled(true);
    }

    @Nested
    @DisplayName("班次时间验证测试")
    class ShiftTimeValidationTests {

        @Test
        @DisplayName("非跨天班次 - 结束时间大于开始时间应该通过")
        void addShift_ValidNonCrossDayShift_ShouldSucceed() {
            // Given
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            when(shiftRepository.save(any(CalendarShift.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CalendarShift result = calendarService.addShift(
                calendarId, "早班", LocalTime.of(8, 0), LocalTime.of(16, 0), 1, 30, false
            );

            // Then
            assertThat(result.getName()).isEqualTo("早班");
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(result.getBreakMinutes()).isEqualTo(30);
            assertThat(result.getNextDay()).isFalse();
        }

        @Test
        @DisplayName("非跨天班次 - 结束时间等于开始时间应该抛出异常")
        void addShift_NonCrossDayShiftWithSameTime_ShouldThrowException() {
            // When & Then - 验证在 findById 之前执行，不需要 stub
            assertThatThrownBy(() ->
                calendarService.addShift(calendarId, "测试班次", LocalTime.of(8, 0), LocalTime.of(8, 0), 1, 0, false)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("结束时间").contains("开始时间"));
        }

        @Test
        @DisplayName("非跨天班次 - 结束时间小于开始时间应该抛出异常")
        void addShift_NonCrossDayShiftWithEndTimeBeforeStartTime_ShouldThrowException() {
            // When & Then - 验证在 findById 之前执行，不需要 stub
            assertThatThrownBy(() ->
                calendarService.addShift(calendarId, "测试班次", LocalTime.of(16, 0), LocalTime.of(8, 0), 1, 0, false)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("结束时间").contains("开始时间"));
        }

        @Test
        @DisplayName("跨天班次 - 结束时间小于开始时间应该通过")
        void addShift_ValidCrossDayShift_ShouldSucceed() {
            // Given
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            when(shiftRepository.save(any(CalendarShift.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            CalendarShift result = calendarService.addShift(
                calendarId, "夜班", LocalTime.of(20, 0), LocalTime.of(8, 0), 2, 60, true
            );

            // Then
            assertThat(result.getName()).isEqualTo("夜班");
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(20, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.getBreakMinutes()).isEqualTo(60);
            assertThat(result.getNextDay()).isTrue();
        }

        @Test
        @DisplayName("跨天班次 - 结束时间大于开始时间应该抛出异常")
        void addShift_CrossDayShiftWithEndTimeAfterStartTime_ShouldThrowException() {
            // When & Then - 验证在 findById 之前执行，不需要 stub
            assertThatThrownBy(() ->
                calendarService.addShift(calendarId, "测试班次", LocalTime.of(8, 0), LocalTime.of(16, 0), 1, 0, true)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("跨天班次"));
        }

        @Test
        @DisplayName("班次时间为 null 应该抛出异常")
        void addShift_WithNullTime_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() ->
                calendarService.addShift(calendarId, "测试班次", null, LocalTime.of(16, 0), 1, 0, false)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("时间"));
        }

        @Test
        @DisplayName("休息时长不能大于等于班次总时长")
        void addShift_WithBreakMinutesTooLarge_ShouldThrowException() {
            assertThatThrownBy(() ->
                calendarService.addShift(calendarId, "测试班次", LocalTime.of(8, 0), LocalTime.of(16, 0), 1, 480, false)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("休息时长"));
        }

        @Test
        @DisplayName("更新班次 - 班次不属于当前日历时应该抛出异常")
        void updateShift_WhenShiftBelongsToAnotherCalendar_ShouldThrowException() {
            CalendarShift existingShift = new CalendarShift();
            existingShift.setId(shiftId);
            existingShift.setCalendar(anotherCalendar);
            existingShift.setName("早班");
            existingShift.setStartTime(LocalTime.of(8, 0));
            existingShift.setEndTime(LocalTime.of(16, 0));
            existingShift.setBreakMinutes(0);
            existingShift.setNextDay(false);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existingShift));

            assertThatThrownBy(() ->
                calendarService.updateShift(calendarId, shiftId, "早班", LocalTime.of(8, 0), LocalTime.of(16, 0), 1, 0, false)
            )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("班次不存在");
        }

        @Test
        @DisplayName("更新班次 - 应该验证新的时间设置")
        void updateShift_ShouldValidateNewTimeSettings() {
            // Given
            CalendarShift existingShift = new CalendarShift();
            existingShift.setId(shiftId);
            existingShift.setCalendar(testCalendar);
            existingShift.setName("早班");
            existingShift.setStartTime(LocalTime.of(8, 0));
            existingShift.setEndTime(LocalTime.of(16, 0));
            existingShift.setBreakMinutes(0);
            existingShift.setNextDay(false);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existingShift));

            // When & Then - 异常在 save 之前抛出，不需要 stub save
            assertThatThrownBy(() ->
                calendarService.updateShift(calendarId, shiftId, "早班", LocalTime.of(16, 0), LocalTime.of(8, 0), null, null, false)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> assertThat(e.getMessage()).contains("结束时间").contains("开始时间"));
        }
        @Test
        @DisplayName("更新班次 - 班次属于当前日历时应该成功")
        void updateShift_WhenShiftBelongsToCurrentCalendar_ShouldSucceed() {
            CalendarShift existingShift = new CalendarShift();
            existingShift.setId(shiftId);
            existingShift.setCalendar(testCalendar);
            existingShift.setName("早班");
            existingShift.setStartTime(LocalTime.of(8, 0));
            existingShift.setEndTime(LocalTime.of(16, 0));
            existingShift.setBreakMinutes(0);
            existingShift.setNextDay(false);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existingShift));
            when(shiftRepository.save(any(CalendarShift.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CalendarShift result = calendarService.updateShift(
                calendarId, shiftId, "晚班", LocalTime.of(9, 0), LocalTime.of(17, 0), 2, 30, false
            );

            assertThat(result.getName()).isEqualTo("晚班");
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(result.getSortOrder()).isEqualTo(2);
            assertThat(result.getBreakMinutes()).isEqualTo(30);
            verify(shiftRepository).save(existingShift);
        }
    }

    @Nested
    @DisplayName("班次删除测试")
    class ShiftDeleteTests {

        @Test
        @DisplayName("删除班次 - 班次不属于当前日历时应该抛出异常")
        void deleteShift_WhenShiftBelongsToAnotherCalendar_ShouldThrowException() {
            CalendarShift existingShift = new CalendarShift();
            existingShift.setId(shiftId);
            existingShift.setCalendar(anotherCalendar);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existingShift));

            assertThatThrownBy(() -> calendarService.deleteShift(calendarId, shiftId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("班次不存在");
        }

        @Test
        @DisplayName("删除班次 - 班次属于当前日历时应该成功")
        void deleteShift_WhenShiftBelongsToCurrentCalendar_ShouldSucceed() {
            CalendarShift existingShift = new CalendarShift();
            existingShift.setId(shiftId);
            existingShift.setCalendar(testCalendar);

            when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(existingShift));

            calendarService.deleteShift(calendarId, shiftId);

            verify(shiftRepository).deleteById(shiftId);
        }
    }

    @Nested
    @DisplayName("日期年份校验测试")
    class DateYearValidationTests {

        @Test
        @DisplayName("查询月份 - 查询年份与日历年份不一致时应抛出异常")
        void getDatesByMonth_WhenYearMismatch_ShouldThrowException() {
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));

            assertThatThrownBy(() -> calendarService.getDatesByMonth(calendarId, 2025, 1))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("查询年份与日历年份不一致");
        }

        @Test
        @DisplayName("更新日期 - 日期不属于日历年份时应抛出异常")
        void updateDateType_WhenDateOutOfCalendarYear_ShouldThrowException() {
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));

            assertThatThrownBy(() -> calendarService.updateDateType(calendarId, java.time.LocalDate.of(2025, 1, 1), DateType.WORKDAY, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("日期不属于日历年份");
        }

        @Test
        @DisplayName("批量更新日期 - 包含跨年日期时应抛出异常")
        void batchUpdateDates_WhenDatesOutOfCalendarYear_ShouldThrowException() {
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));

            assertThatThrownBy(() -> calendarService.batchUpdateDates(
                calendarId,
                List.of(java.time.LocalDate.of(2024, 1, 1), java.time.LocalDate.of(2025, 1, 1)),
                DateType.HOLIDAY,
                "测试"
            ))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("日期不属于日历年份");
        }
    }

    @Nested
    @DisplayName("批量更新测试")
    class BatchUpdateTests {

        @Test
        @DisplayName("批量更新日期类型 - 空列表应该直接返回")
        void batchUpdateDates_WithEmptyList_ShouldReturnEarly() {
            // When
            calendarService.batchUpdateDates(calendarId, List.of(), DateType.HOLIDAY, "测试");

            // Then
            verify(dateRepository, never()).batchUpdateDateTypes(any(), any(), any(), any());
        }

        @Test
        @DisplayName("批量更新日期类型 - null 列表应该直接返回")
        void batchUpdateDates_WithNullList_ShouldReturnEarly() {
            // When
            calendarService.batchUpdateDates(calendarId, null, DateType.HOLIDAY, "测试");

            // Then
            verify(dateRepository, never()).batchUpdateDateTypes(any(), any(), any(), any());
        }

        @Test
        @DisplayName("批量更新日期类型 - 应该调用批量更新方法")
        void batchUpdateDates_WithValidList_ShouldCallBatchUpdate() {
            // Given
            List<java.time.LocalDate> dates = List.of(
                java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 1, 2)
            );
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            when(dateRepository.batchUpdateDateTypes(eq(calendarId), eq(dates), eq(DateType.HOLIDAY), eq("测试")))
                .thenReturn(2);

            // When
            calendarService.batchUpdateDates(calendarId, dates, DateType.HOLIDAY, "测试");

            // Then
            verify(dateRepository).batchUpdateDateTypes(calendarId, dates, DateType.HOLIDAY, "测试");
        }
    }

    @Nested
    @DisplayName("单双休模式测试")
    class WeekendPatternTests {

        @Test
        @DisplayName("应用单休模式 - 周日应该被标记为休息日")
        void applyWeekendPattern_Single_ShouldMarkSundayAsRestDay() {
            // Given
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            List<CalendarDate> dates = createTestDates();
            when(dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(dates);

            // When
            calendarService.applyWeekendPattern(calendarId, "SINGLE");

            // Then
            verify(dateRepository, times(2)).updateDateType(eq(calendarId), any(), eq(DateType.RESTDAY), isNull());
        }

        @Test
        @DisplayName("应用双休模式 - 周六和周日应该被标记为休息日")
        void applyWeekendPattern_Double_ShouldMarkSaturdayAndSundayAsRestDay() {
            // Given
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            List<CalendarDate> dates = createTestDates();
            when(dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(dates);

            // When
            calendarService.applyWeekendPattern(calendarId, "DOUBLE");

            // Then
            verify(dateRepository, times(4)).updateDateType(eq(calendarId), any(), eq(DateType.RESTDAY), isNull());
        }

        @Test
        @DisplayName("空模式应该抛出异常")
        void applyWeekendPattern_EmptyPattern_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> calendarService.applyWeekendPattern(calendarId, ""))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("单双休模式不能为空");
        }

        @Test
        @DisplayName("无效模式应该抛出异常")
        void applyWeekendPattern_InvalidPattern_ShouldThrowException() {
            // Given
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            List<CalendarDate> dates = createTestDates();
            when(dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(dates);

            // When & Then
            assertThatThrownBy(() -> calendarService.applyWeekendPattern(calendarId, "INVALID"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("不支持的单双休模式");
        }

        private List<CalendarDate> createTestDates() {
            return java.time.LocalDate.of(2024, 1, 1).datesUntil(java.time.LocalDate.of(2024, 1, 15))
                .map(date -> {
                    CalendarDate cd = new CalendarDate();
                    cd.setDate(date);
                    cd.setDateType(DateType.WORKDAY);
                    cd.setCalendar(testCalendar);
                    return cd;
                })
                .toList();
        }
    }

    @Nested
    @DisplayName("资源不存在测试")
    class ResourceNotFoundTests {

        @Test
        @DisplayName("查询不存在的日历应该抛出异常")
        void getCalendarById_NotFound_ShouldThrowException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(calendarRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarService.getCalendarById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("日历不存在");
        }

        @Test
        @DisplayName("更新不存在的班次应该抛出异常")
        void updateShift_NotFound_ShouldThrowException() {
            // Given
            UUID nonExistentShiftId = UUID.randomUUID();
            when(shiftRepository.findById(nonExistentShiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                calendarService.updateShift(calendarId, nonExistentShiftId, "测试", LocalTime.of(8, 0), LocalTime.of(16, 0), 1, 0, false)
            )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("班次不存在");
        }

        @Test
        @DisplayName("删除不存在的班次应该抛出异常")
        void deleteShift_NotFound_ShouldThrowException() {
            // Given
            UUID nonExistentShiftId = UUID.randomUUID();
            when(shiftRepository.findById(nonExistentShiftId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> calendarService.deleteShift(calendarId, nonExistentShiftId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("班次不存在");
        }
    }

    @Nested
    @DisplayName("日历CRUD测试")
    class CalendarCrudTests {

        @Test
        @DisplayName("创建日历 - 编码已存在应该抛出异常")
        void createCalendar_DuplicateCode_ShouldThrowException() {
            // Given
            when(calendarRepository.existsByCode("DUPLICATE")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() ->
                calendarService.createCalendar("测试", "DUPLICATE", 2024, "描述")
            )
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("日历编码已存在");
        }

        @Test
        @DisplayName("设置默认日历 - 应该取消旧的默认")
        void setDefaultCalendar_ShouldUnsetOldDefault() {
            // Given
            FactoryCalendar oldDefault = new FactoryCalendar();
            oldDefault.setId(UUID.randomUUID());
            oldDefault.setIsDefault(true);

            when(calendarRepository.findByIsDefaultTrue()).thenReturn(Optional.of(oldDefault));
            when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(testCalendar));
            when(calendarRepository.save(any(FactoryCalendar.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            calendarService.setDefaultCalendar(calendarId);

            // Then
            assertThat(oldDefault.getIsDefault()).isFalse();
            assertThat(testCalendar.getIsDefault()).isTrue();
            verify(calendarRepository, times(2)).save(any(FactoryCalendar.class));
        }

        @Test
        @DisplayName("统计工作日 - 应该正确计数")
        void countYearWorkdays_ShouldReturnCorrectCount() {
            // Given
            List<CalendarDate> dates = java.time.LocalDate.of(2024, 1, 1)
                .datesUntil(java.time.LocalDate.of(2024, 1, 11))
                .map(date -> {
                    CalendarDate cd = new CalendarDate();
                    cd.setDate(date);
                    // 前5天是工作日，后5天是休息日
                    cd.setDateType(date.getDayOfMonth() <= 5 ? DateType.WORKDAY : DateType.RESTDAY);
                    return cd;
                })
                .toList();

            when(dateRepository.findByCalendarIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(dates);

            // When
            long count = calendarService.countYearWorkdays(calendarId, 2024);

            // Then
            assertThat(count).isEqualTo(5);
        }
    }
}
