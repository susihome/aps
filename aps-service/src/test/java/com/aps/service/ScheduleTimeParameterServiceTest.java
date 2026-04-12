package com.aps.service;

import com.aps.domain.entity.Resource;
import com.aps.domain.entity.ScheduleTimeParameter;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.ResourceRepository;
import com.aps.service.repository.ScheduleTimeParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("排程时间参数服务测试")
class ScheduleTimeParameterServiceTest {

    @Mock
    private ScheduleTimeParameterRepository repo;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ScheduleTimeParameterService service;

    private UUID resourceId;
    private ScheduleTimeParameter resourceParam;
    private ScheduleTimeParameter globalDefault;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID();

        resourceParam = new ScheduleTimeParameter();
        resourceParam.setId(UUID.randomUUID());
        resourceParam.setOrderFilterStartDays(12);
        resourceParam.setOrderFilterStartTime(LocalTime.of(8, 0));
        resourceParam.setOrderFilterEndDays(42);
        resourceParam.setOrderFilterEndTime(LocalTime.of(0, 0));
        resourceParam.setPlanningStartDays(12);
        resourceParam.setPlanningStartTime(LocalTime.of(9, 0));
        resourceParam.setDisplayStartDays(11);
        resourceParam.setDisplayEndDays(43);
        resourceParam.setCompletionDays(5);
        resourceParam.setTimeScale(1);
        resourceParam.setFactor(0);
        resourceParam.setExceedPeriod(2);
        resourceParam.setIsDefault(false);
        resourceParam.setEnabled(true);

        globalDefault = new ScheduleTimeParameter();
        globalDefault.setId(UUID.randomUUID());
        globalDefault.setOrderFilterStartDays(0);
        globalDefault.setOrderFilterStartTime(LocalTime.of(8, 0));
        globalDefault.setOrderFilterEndDays(14);
        globalDefault.setOrderFilterEndTime(LocalTime.of(0, 0));
        globalDefault.setPlanningStartDays(0);
        globalDefault.setPlanningStartTime(LocalTime.of(9, 0));
        globalDefault.setDisplayStartDays(0);
        globalDefault.setDisplayEndDays(30);
        globalDefault.setCompletionDays(3);
        globalDefault.setTimeScale(1);
        globalDefault.setFactor(0);
        globalDefault.setExceedPeriod(null);
        globalDefault.setIsDefault(true);
        globalDefault.setEnabled(true);
    }

    @Nested
    @DisplayName("参数查找优先级测试")
    class FindEffectiveParamTests {

        @Test
        @DisplayName("Resource 级别配置优先于全局默认")
        void findEffectiveParam_ResourceSpecific_TakesPriority() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameter result = service.findEffectiveParam(resourceId);

            assertThat(result).isSameAs(resourceParam);
            verify(repo, never()).findByIsDefaultTrueAndEnabledTrue();
        }

        @Test
        @DisplayName("无 Resource 配置时回退到全局默认")
        void findEffectiveParam_NoResourceConfig_FallsBackToGlobalDefault() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.empty());
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.of(globalDefault));

            ScheduleTimeParameter result = service.findEffectiveParam(resourceId);

            assertThat(result).isSameAs(globalDefault);
        }

        @Test
        @DisplayName("无任何配置时使用系统兜底默认值")
        void findEffectiveParam_NoConfig_UsesSystemDefault() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.empty());
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.empty());

            ScheduleTimeParameter result = service.findEffectiveParam(resourceId);

            assertThat(result.getOrderFilterStartDays()).isEqualTo(0);
            assertThat(result.getOrderFilterEndDays()).isEqualTo(14);
            assertThat(result.getPlanningStartDays()).isEqualTo(0);
            assertThat(result.getDisplayEndDays()).isEqualTo(30);
            assertThat(result.getTimeScale()).isEqualTo(1);
        }

        @Test
        @DisplayName("resourceId 为 null 时直接使用全局默认")
        void findEffectiveParam_NullResourceId_UsesGlobalDefault() {
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.of(globalDefault));

            ScheduleTimeParameter result = service.findEffectiveParam(null);

            assertThat(result).isSameAs(globalDefault);
            verify(repo, never()).findByResource_IdAndEnabledTrue(any());
        }

        @Test
        @DisplayName("配置 enabled=false 时不被选中，回退到全局默认")
        void findEffectiveParam_DisabledResourceConfig_FallsBack() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.empty());
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.of(globalDefault));

            ScheduleTimeParameter result = service.findEffectiveParam(resourceId);

            assertThat(result).isSameAs(globalDefault);
        }
    }

    @Nested
    @DisplayName("工单筛选范围计算测试")
    class OrderFilterWindowTests {

        @Test
        @DisplayName("正常计算工单筛选范围")
        void getOrderFilterWindow_Normal() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.OrderFilterWindow window =
                    service.getOrderFilterWindow(resourceId);

            LocalDate today = LocalDate.now();
            assertThat(window.start()).isEqualTo(today.plusDays(12).atTime(8, 0));
            assertThat(window.end()).isEqualTo(today.plusDays(42).atTime(0, 0));
        }

        @Test
        @DisplayName("跨月边界：+12天的时间精确到时分")
        void getOrderFilterWindow_CrossMonthBoundary() {
            resourceParam.setOrderFilterStartDays(12);
            resourceParam.setOrderFilterStartTime(LocalTime.of(8, 0));
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.OrderFilterWindow window =
                    service.getOrderFilterWindow(resourceId);

            assertThat(window.start().toLocalDate()).isEqualTo(LocalDate.now().plusDays(12));
            assertThat(window.start().getHour()).isEqualTo(8);
            assertThat(window.start().getMinute()).isEqualTo(0);
        }

        @Test
        @DisplayName("跨年边界：startDays=0 时从今天开始")
        void getOrderFilterWindow_StartDaysZero_StartsToday() {
            globalDefault.setOrderFilterStartDays(0);
            globalDefault.setOrderFilterStartTime(LocalTime.of(8, 0));
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.empty());
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.of(globalDefault));

            ScheduleTimeParameterService.OrderFilterWindow window =
                    service.getOrderFilterWindow(resourceId);

            assertThat(window.start().toLocalDate()).isEqualTo(LocalDate.now());
            assertThat(window.start().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
        }

        @Test
        @DisplayName("筛选范围与显示范围不同（看多远 ≠ 捞工单范围）")
        void getOrderFilterWindow_DifferentFromDisplayWindow() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.OrderFilterWindow orderWindow =
                    service.getOrderFilterWindow(resourceId);
            ScheduleTimeParameterService.DisplayWindow displayWindow =
                    service.getDisplayWindow(resourceId);

            assertThat(orderWindow.start().toLocalDate())
                    .isNotEqualTo(displayWindow.start());
            assertThat(orderWindow.end().toLocalDate())
                    .isNotEqualTo(displayWindow.end());
        }
    }

    @Nested
    @DisplayName("排程安排起点计算测试")
    class PlanningStartTests {

        @Test
        @DisplayName("planningStart 正确计算")
        void getPlanningStart_Normal() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            LocalDateTime planningStart = service.getPlanningStart(resourceId);

            LocalDate today = LocalDate.now();
            assertThat(planningStart).isEqualTo(today.plusDays(12).atTime(9, 0));
        }

        @Test
        @DisplayName("planningStart 可晚于 orderFilterStart（同天不同时间）")
        void getPlanningStart_CanBeLaterThanOrderFilterStart() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.OrderFilterWindow orderWindow =
                    service.getOrderFilterWindow(resourceId);
            LocalDateTime planningStart = service.getPlanningStart(resourceId);

            assertThat(planningStart).isAfterOrEqualTo(orderWindow.start());
        }

        @Test
        @DisplayName("大天数跨年计算正确")
        void getPlanningStart_LargeDays_CrossesYear() {
            resourceParam.setPlanningStartDays(365);
            resourceParam.setPlanningStartTime(LocalTime.of(9, 0));
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            LocalDateTime planningStart = service.getPlanningStart(resourceId);

            assertThat(planningStart.toLocalDate())
                    .isEqualTo(LocalDate.now().plusDays(365));
        }
    }

    @Nested
    @DisplayName("显示窗口计算测试")
    class DisplayWindowTests {

        @Test
        @DisplayName("显示窗口正确计算")
        void getDisplayWindow_Normal() {
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.DisplayWindow window =
                    service.getDisplayWindow(resourceId);

            LocalDate today = LocalDate.now();
            assertThat(window.start()).isEqualTo(today.plusDays(11));
            assertThat(window.end()).isEqualTo(today.plusDays(43));
        }

        @Test
        @DisplayName("显示开始天支持负数，可展示今天之前日期")
        void getDisplayWindow_NegativeDisplayStartDays_Supported() {
            resourceParam.setDisplayStartDays(-2);
            resourceParam.setDisplayEndDays(14);
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            ScheduleTimeParameterService.DisplayWindow window =
                    service.getDisplayWindow(resourceId);

            LocalDate today = LocalDate.now();
            assertThat(window.start()).isEqualTo(today.minusDays(2));
            assertThat(window.end()).isEqualTo(today.plusDays(14));
        }
    }

    @Nested
    @DisplayName("完成期限计算测试")
    class CompletionDaysTests {

        @Test
        @DisplayName("completionDays + exceedPeriod 正确求和")
        void getEffectiveCompletionDays_WithExceedPeriod() {
            resourceParam.setCompletionDays(5);
            resourceParam.setExceedPeriod(2);
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.of(resourceParam));

            int result = service.getEffectiveCompletionDays(resourceId);

            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("exceedPeriod 为 null 时等同于 0")
        void getEffectiveCompletionDays_NullExceedPeriod_TreatedAsZero() {
            globalDefault.setCompletionDays(3);
            globalDefault.setExceedPeriod(null);
            when(repo.findByResource_IdAndEnabledTrue(resourceId))
                    .thenReturn(Optional.empty());
            when(repo.findByIsDefaultTrueAndEnabledTrue())
                    .thenReturn(Optional.of(globalDefault));

            int result = service.getEffectiveCompletionDays(resourceId);

            assertThat(result).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("orderFilterEndDays < orderFilterStartDays 应抛出异常")
        void create_EndDaysBeforeStartDays_ShouldThrow() {
            assertThatThrownBy(() -> service.create(
                    null,
                    10, LocalTime.of(8, 0),
                    5, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    0, 30,
                    0, 1, 0, null,
                    false, true, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("筛选终止天数");
        }

        @Test
        @DisplayName("displayEndDays <= displayStartDays 应抛出异常")
        void create_DisplayEndNotAfterStart_ShouldThrow() {
            assertThatThrownBy(() -> service.create(
                    null,
                    0, LocalTime.of(8, 0),
                    14, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    10, 10,
                    0, 1, 0, null,
                    false, true, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("显示结束天数");
        }

        @Test
        @DisplayName("timeScale < 1 应抛出异常")
        void create_TimeScaleLessThanOne_ShouldThrow() {
            assertThatThrownBy(() -> service.create(
                    null,
                    0, LocalTime.of(8, 0),
                    14, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    0, 30,
                    0, 0, 0, null,
                    false, true, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("时间刻度");
        }

        @Test
        @DisplayName("exceedPeriod < 0 应抛出异常")
        void create_NegativeExceedPeriod_ShouldThrow() {
            assertThatThrownBy(() -> service.create(
                    null,
                    0, LocalTime.of(8, 0),
                    14, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    0, 30,
                    0, 1, 0, -1,
                    false, true, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("超出期间");
        }

        @Test
        @DisplayName("重复创建同一 Resource 的配置应抛出冲突异常")
        void create_DuplicateResource_ShouldThrow() {
            when(repo.existsByResource_Id(resourceId)).thenReturn(true);

            assertThatThrownBy(() -> service.create(
                    resourceId,
                    0, LocalTime.of(8, 0),
                    14, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    0, 30,
                    0, 1, 0, null,
                    false, true, null))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("已存在");
        }

        @Test
        @DisplayName("重复创建全局默认应抛出冲突异常")
        void create_DuplicateGlobalDefault_ShouldThrow() {
            when(repo.existsByIsDefaultTrue()).thenReturn(true);

            assertThatThrownBy(() -> service.create(
                    null,
                    0, LocalTime.of(8, 0),
                    14, LocalTime.of(0, 0),
                    0, LocalTime.of(9, 0),
                    0, 30,
                    0, 1, 0, null,
                    true, true, null))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("全局默认");
        }

        @Test
        @DisplayName("删除不存在的参数应抛出 ResourceNotFoundException")
        void delete_NotFound_ShouldThrow() {
            UUID id = UUID.randomUUID();
            when(repo.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("排程时间参数不存在");
        }
    }
}
