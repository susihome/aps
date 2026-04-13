package com.aps.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.aps.domain.entity.Schedule;
import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.SolverTaskStatus;
import com.aps.domain.enums.SolverTaskType;
import com.aps.domain.enums.TriggerSource;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.mq.ScheduleNotificationDispatcher;
import com.aps.service.mq.ScheduleTaskDispatcher;
import com.aps.service.repository.ScheduleRepository;
import com.aps.service.repository.ScheduleSolverTaskRepository;
import com.aps.solver.converter.ScheduleModelConverter;
import com.aps.solver.model.SchedulePlanningModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产服务测试")
class ScheduleServiceTest {

    @Mock
    private SolverManager<SchedulePlanningModel, UUID> solverManager;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ScheduleModelConverter modelConverter;

    @Mock
    private ScheduleSolverTaskRepository scheduleSolverTaskRepository;

    @Mock
    private SolverJob<SchedulePlanningModel, UUID> solverJob;

    @Mock
    private ScheduleLockService scheduleLockService;

    @Mock
    private ScheduleLockRenewalService scheduleLockRenewalService;

    @Mock
    private ScheduleTaskDispatcher scheduleTaskDispatcher;

    @Mock
    private ScheduleNotificationDispatcher scheduleNotificationDispatcher;

    @Mock
    private Executor scheduleTaskExecutor;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("提交排产任务时应创建运行中任务")
    void submitSolveTask_shouldCreateRunningTask() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        AtomicReference<SolverTaskStatus> firstSavedStatus = new AtomicReference<>();
        AtomicReference<LocalDateTime> firstStartedAt = new AtomicReference<>();
        AtomicReference<String> firstLockOwnerToken = new AtomicReference<>();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.tryLock(org.mockito.ArgumentMatchers.eq(scheduleId), anyString())).thenReturn(true);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> {
                    ScheduleSolverTask saved = invocation.getArgument(0);
                    firstSavedStatus.compareAndSet(null, saved.getStatus());
                    firstStartedAt.compareAndSet(null, saved.getStartedAt());
                    firstLockOwnerToken.compareAndSet(null, saved.getLockOwnerToken());
                    return saved;
                });

        ScheduleSolverTask task = scheduleService.submitSolveTask(scheduleId, null, TriggerSource.MANUAL);

        assertThat(task.getScheduleId()).isEqualTo(scheduleId);
        assertThat(task.getTaskType()).isEqualTo(SolverTaskType.PLAN);
        assertThat(task.getTriggerSource()).isEqualTo(TriggerSource.MANUAL);
        assertThat(task.getStatus()).isEqualTo(SolverTaskStatus.PENDING);

        assertThat(firstStartedAt.get()).isNull();
        assertThat(firstSavedStatus.get()).isEqualTo(SolverTaskStatus.PENDING);
        assertThat(firstLockOwnerToken.get()).isNotBlank();
        verify(scheduleTaskDispatcher).dispatch(task.getId(), scheduleId);
    }

    @Test
    @DisplayName("排产锁未获取成功时应拒绝重复提交")
    void submitSolveTask_whenLockNotAcquired_shouldThrowConflict() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.tryLock(org.mockito.ArgumentMatchers.eq(scheduleId), anyString())).thenReturn(false);

        assertThatThrownBy(() -> scheduleService.submitSolveTask(scheduleId, null, TriggerSource.MANUAL))
                .isInstanceOf(com.aps.service.exception.BusinessException.class)
                .hasMessageContaining("正在求解中");
    }

    @Test
    @DisplayName("worker 执行排产任务时应将任务置为运行中")
    void executeSolveTask_shouldMarkTaskRunning() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.PENDING);
        task.setLockOwnerToken("owner-token");
        SchedulePlanningModel planningModel = new SchedulePlanningModel();
        AtomicReference<SolverTaskStatus> firstSavedStatus = new AtomicReference<>();
        AtomicReference<LocalDateTime> firstStartedAt = new AtomicReference<>();

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.isOwnedBy(scheduleId, "owner-token")).thenReturn(true);
        when(modelConverter.toPlanningModel(schedule)).thenReturn(planningModel);
        when(solverManager.solveAndListen(eq(scheduleId), eq(planningModel), any()))
                .thenReturn(solverJob);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> {
                    ScheduleSolverTask saved = invocation.getArgument(0);
                    firstSavedStatus.compareAndSet(null, saved.getStatus());
                    firstStartedAt.compareAndSet(null, saved.getStartedAt());
                    return saved;
                });
        lenient().when(solverJob.getFinalBestSolution()).thenReturn(planningModel);
        lenient().when(scheduleRepository.save(schedule)).thenReturn(schedule);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(scheduleTaskExecutor).execute(any(Runnable.class));

        scheduleService.executeSolveTask(taskId);

        assertThat(firstSavedStatus.get()).isEqualTo(SolverTaskStatus.RUNNING);
        assertThat(firstStartedAt.get()).isNotNull();
        verify(scheduleLockRenewalService).start(eq(scheduleId), eq("owner-token"), any(Runnable.class));
        verify(scheduleLockRenewalService).stop(scheduleId);
        verify(scheduleLockService).unlock(scheduleId, "owner-token");
        verify(scheduleNotificationDispatcher).publishStarted(scheduleId);
        verify(scheduleNotificationDispatcher).publishProgress(scheduleId, 20, "正在构建求解模型", null);
        verify(scheduleNotificationDispatcher).publishProgress(scheduleId, 40, "求解任务已提交，等待结果", null);
        verify(scheduleNotificationDispatcher).publishProgress(scheduleId, 80, "求解完成，正在保存结果", finalScoreOf(planningModel));
    }

    @Test
    @DisplayName("worker 执行时应推送中间最优解 score")
    void executeSolveTask_shouldPublishIntermediateBestScore() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.PENDING);
        task.setLockOwnerToken("owner-token");
        SchedulePlanningModel planningModel = new SchedulePlanningModel();
        SchedulePlanningModel intermediateBestSolution = new SchedulePlanningModel();
        intermediateBestSolution.setScore(HardSoftScore.of(0, -20));

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.isOwnedBy(scheduleId, "owner-token")).thenReturn(true);
        when(modelConverter.toPlanningModel(schedule)).thenReturn(planningModel);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(solverManager.solveAndListen(eq(scheduleId), eq(planningModel), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Consumer<SchedulePlanningModel> bestSolutionConsumer = invocation.getArgument(2, Consumer.class);
                    bestSolutionConsumer.accept(intermediateBestSolution);
                    return solverJob;
                });
        when(solverJob.getFinalBestSolution()).thenReturn(intermediateBestSolution);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(scheduleTaskExecutor).execute(any(Runnable.class));

        scheduleService.executeSolveTask(taskId);

        verify(scheduleNotificationDispatcher)
                .publishProgress(scheduleId, 50, "求解中，发现更优解", "0hard/-20soft");
    }

    @Test
    @DisplayName("查询最新排产任务时应返回最近记录")
    void getLatestSolverTask_shouldReturnLatestTask() {
        UUID scheduleId = UUID.randomUUID();
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(UUID.randomUUID());
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.SUCCESS);

        when(scheduleSolverTaskRepository.findFirstByScheduleIdOrderByCreateTimeDesc(scheduleId))
                .thenReturn(Optional.of(task));

        ScheduleSolverTask result = scheduleService.getLatestSolverTask(scheduleId);

        assertThat(result).isSameAs(task);
    }

    @Test
    @DisplayName("查询排产任务历史时应返回最近任务列表")
    void listSolverTasks_shouldReturnRecentTasks() {
        UUID scheduleId = UUID.randomUUID();
        ScheduleSolverTask firstTask = new ScheduleSolverTask();
        firstTask.setId(UUID.randomUUID());
        firstTask.setScheduleId(scheduleId);
        firstTask.setStatus(SolverTaskStatus.FAILED);
        ScheduleSolverTask secondTask = new ScheduleSolverTask();
        secondTask.setId(UUID.randomUUID());
        secondTask.setScheduleId(scheduleId);
        secondTask.setStatus(SolverTaskStatus.SUCCESS);

        when(scheduleSolverTaskRepository.findTop10ByScheduleIdOrderByCreateTimeDesc(scheduleId))
                .thenReturn(List.of(firstTask, secondTask));

        List<ScheduleSolverTask> result = scheduleService.listSolverTasks(scheduleId);

        assertThat(result).containsExactly(firstTask, secondTask);
    }

    @Test
    @DisplayName("按任务ID查询排产任务时应返回任务详情")
    void getSolverTask_shouldReturnTask() {
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(UUID.randomUUID());
        task.setStatus(SolverTaskStatus.FAILED);
        task.setErrorMessage("solver failed");

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        ScheduleSolverTask result = scheduleService.getSolverTask(taskId);

        assertThat(result).isSameAs(task);
    }

    @Test
    @DisplayName("查询不存在的最新排产任务时应抛出未找到异常")
    void getLatestSolverTask_whenMissing_shouldThrowNotFound() {
        UUID scheduleId = UUID.randomUUID();
        when(scheduleSolverTaskRepository.findFirstByScheduleIdOrderByCreateTimeDesc(scheduleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.getLatestSolverTask(scheduleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("未找到排产任务");
    }

    @Test
    @DisplayName("按任务ID查询不存在的排产任务时应抛出未找到异常")
    void getSolverTask_whenMissing_shouldThrowNotFound() {
        UUID taskId = UUID.randomUUID();
        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.getSolverTask(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("排产任务不存在");
    }

    @Test
    @DisplayName("重试失败任务时应重新提交排产任务")
    void retryFailedTask_shouldSubmitNewTask() {
        UUID scheduleId = UUID.randomUUID();
        UUID failedTaskId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        ScheduleSolverTask failedTask = new ScheduleSolverTask();
        failedTask.setId(failedTaskId);
        failedTask.setScheduleId(scheduleId);
        failedTask.setStatus(SolverTaskStatus.FAILED);
        failedTask.setTaskType(SolverTaskType.PLAN);
        failedTask.setTriggerSource(TriggerSource.MANUAL);

        when(scheduleSolverTaskRepository.findById(failedTaskId)).thenReturn(Optional.of(failedTask));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.tryLock(org.mockito.ArgumentMatchers.eq(scheduleId), anyString())).thenReturn(true);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleSolverTask retriedTask = scheduleService.retryFailedTask(failedTaskId);

        assertThat(retriedTask.getId()).isNotEqualTo(failedTaskId);
        assertThat(retriedTask.getScheduleId()).isEqualTo(scheduleId);
        assertThat(retriedTask.getStatus()).isEqualTo(SolverTaskStatus.PENDING);
        verify(scheduleTaskDispatcher).dispatch(retriedTask.getId(), scheduleId);
    }

    @Test
    @DisplayName("重试非失败任务时应拒绝")
    void retryFailedTask_whenTaskNotFailed_shouldReject() {
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(UUID.randomUUID());
        task.setStatus(SolverTaskStatus.RUNNING);

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> scheduleService.retryFailedTask(taskId))
                .isInstanceOf(com.aps.service.exception.BusinessException.class)
                .hasMessageContaining("仅允许重试失败");
    }

    @Test
    @DisplayName("worker 执行失败时应停止锁续期")
    void executeSolveTask_whenFailed_shouldStopLockRenewal() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.PENDING);
        task.setLockOwnerToken("owner-token");
        SchedulePlanningModel planningModel = new SchedulePlanningModel();

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.isOwnedBy(scheduleId, "owner-token")).thenReturn(true);
        when(modelConverter.toPlanningModel(schedule)).thenReturn(planningModel);
        when(solverManager.solveAndListen(eq(scheduleId), eq(planningModel), any()))
                .thenReturn(solverJob);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(solverJob.getFinalBestSolution()).thenThrow(new IllegalStateException("solver failed"));
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(scheduleTaskExecutor).execute(any(Runnable.class));

        scheduleService.executeSolveTask(taskId);

        verify(scheduleLockRenewalService).start(eq(scheduleId), eq("owner-token"), any(Runnable.class));
        verify(scheduleLockRenewalService).stop(scheduleId);
        verify(scheduleLockService).unlock(scheduleId, "owner-token");
        verify(scheduleNotificationDispatcher).publishProgress(scheduleId, 20, "正在构建求解模型", null);
        verify(scheduleNotificationDispatcher).publishProgress(scheduleId, 40, "求解任务已提交，等待结果", null);
        verify(scheduleNotificationDispatcher).publishFailed(scheduleId, "solver failed");
    }

    @Test
    @DisplayName("worker 启动前若锁已不归自己所有则不应继续求解")
    void executeSolveTask_whenLockOwnershipLost_shouldFailFast() {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Schedule schedule = new Schedule();
        schedule.setId(scheduleId);
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.PENDING);
        task.setLockOwnerToken("owner-token");

        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleLockService.isOwnedBy(scheduleId, "owner-token")).thenReturn(false);
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduleService.executeSolveTask(taskId);

        verify(scheduleNotificationDispatcher).publishFailed(scheduleId, "排产锁已失效，任务已取消执行");
        verify(solverManager, never()).solveAndListen(any(), any(SchedulePlanningModel.class), any());
        verify(scheduleLockRenewalService, never()).start(any(), any(), any(Runnable.class));
    }

    @Test
    @DisplayName("运行中锁丢失时应主动终止求解")
    void handleLockLostDuringSolve_shouldTerminateSolve() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setStatus(SolverTaskStatus.RUNNING);
        task.setLockOwnerToken("owner-token");
        when(scheduleSolverTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        activeSolverJobsPut(scheduleId);

        invokeHandleLockLostDuringSolve(taskId, scheduleId, "owner-token");

        verify(solverManager).terminateEarly(scheduleId);
        verify(scheduleLockService).unlock(scheduleId, "owner-token");
        verify(scheduleNotificationDispatcher).publishFailed(scheduleId, "排产锁已失效，求解已终止");
    }

    @Test
    @DisplayName("停止求解时应使用运行中任务的 owner token 解锁")
    void stopSolving_shouldUnlockWithRunningTaskOwnerToken() {
        UUID scheduleId = UUID.randomUUID();
        ScheduleSolverTask runningTask = new ScheduleSolverTask();
        runningTask.setId(UUID.randomUUID());
        runningTask.setScheduleId(scheduleId);
        runningTask.setStatus(SolverTaskStatus.RUNNING);
        runningTask.setLockOwnerToken("owner-token");

        activeSolverJobsPut(scheduleId);
        when(scheduleSolverTaskRepository.findFirstByScheduleIdAndStatusOrderByCreateTimeDesc(
                scheduleId,
                SolverTaskStatus.RUNNING)).thenReturn(Optional.of(runningTask));

        scheduleService.stopSolving(scheduleId);

        verify(scheduleLockRenewalService).stop(scheduleId);
        verify(scheduleLockService).unlock(scheduleId, "owner-token");
    }

    @SuppressWarnings("unchecked")
    private void activeSolverJobsPut(UUID scheduleId) {
        try {
            java.lang.reflect.Field field = ScheduleService.class.getDeclaredField("activeSolverJobs");
            field.setAccessible(true);
            ((java.util.Map<UUID, SolverJob<SchedulePlanningModel, UUID>>) field.get(scheduleService))
                    .put(scheduleId, solverJob);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private void invokeHandleLockLostDuringSolve(UUID taskId, UUID scheduleId, String ownerToken) throws Exception {
        java.lang.reflect.Method method = ScheduleService.class.getDeclaredMethod(
                "handleLockLostDuringSolve",
                UUID.class,
                UUID.class,
                String.class);
        method.setAccessible(true);
        method.invoke(scheduleService, taskId, scheduleId, ownerToken);
    }

    private String finalScoreOf(SchedulePlanningModel planningModel) {
        return planningModel.getScore() != null ? planningModel.getScore().toString() : "N/A";
    }
}
