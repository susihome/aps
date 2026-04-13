package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.SolverTaskStatus;
import com.aps.domain.enums.SolverTaskType;
import com.aps.domain.enums.TriggerSource;
import com.aps.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产控制器测试")
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("提交排产应返回任务信息")
    void solveSchedule_shouldReturnTaskInfo() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = buildTask(taskId, scheduleId, SolverTaskStatus.RUNNING);
        when(scheduleService.submitSolveTask(scheduleId, null, TriggerSource.MANUAL)).thenReturn(task);

        mockMvc.perform(post("/api/schedules/{id}/solve", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.data.scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    @DisplayName("查询最新排产任务应返回任务状态")
    void getLatestSolverTask_shouldReturnTaskStatus() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = buildTask(taskId, scheduleId, SolverTaskStatus.SUCCESS);
        when(scheduleService.getLatestSolverTask(scheduleId)).thenReturn(task);

        mockMvc.perform(get("/api/schedules/{id}/solver-tasks/latest", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.data.taskType").value("PLAN"))
                .andExpect(jsonPath("$.data.triggerSource").value("MANUAL"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("查询排产任务历史应返回任务列表")
    void listSolverTasks_shouldReturnTaskList() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        ScheduleSolverTask failedTask = buildTask(UUID.randomUUID(), scheduleId, SolverTaskStatus.FAILED);
        failedTask.setErrorMessage("solver failed");
        ScheduleSolverTask successTask = buildTask(UUID.randomUUID(), scheduleId, SolverTaskStatus.SUCCESS);
        when(scheduleService.listSolverTasks(scheduleId)).thenReturn(List.of(failedTask, successTask));

        mockMvc.perform(get("/api/schedules/{id}/solver-tasks", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].taskId").value(failedTask.getId().toString()))
                .andExpect(jsonPath("$.data[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data[0].errorMessage").value("solver failed"))
                .andExpect(jsonPath("$.data[1].taskId").value(successTask.getId().toString()))
                .andExpect(jsonPath("$.data[1].status").value("SUCCESS"));
    }

    @Test
    @DisplayName("查询排产任务详情应返回失败原因和时间信息")
    void getSolverTask_shouldReturnTaskDetail() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ScheduleSolverTask task = buildTask(taskId, scheduleId, SolverTaskStatus.FAILED);
        task.setErrorMessage("solver failed");
        task.setStartedAt(LocalDateTime.of(2026, 4, 13, 12, 0));
        task.setFinishedAt(LocalDateTime.of(2026, 4, 13, 12, 5));
        when(scheduleService.getSolverTask(taskId)).thenReturn(task);

        mockMvc.perform(get("/api/schedules/solver-tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.errorMessage").value("solver failed"))
                .andExpect(jsonPath("$.data.startedAt[0]").value(2026))
                .andExpect(jsonPath("$.data.startedAt[1]").value(4))
                .andExpect(jsonPath("$.data.startedAt[2]").value(13))
                .andExpect(jsonPath("$.data.startedAt[3]").value(12))
                .andExpect(jsonPath("$.data.startedAt[4]").value(0))
                .andExpect(jsonPath("$.data.finishedAt[0]").value(2026))
                .andExpect(jsonPath("$.data.finishedAt[1]").value(4))
                .andExpect(jsonPath("$.data.finishedAt[2]").value(13))
                .andExpect(jsonPath("$.data.finishedAt[3]").value(12))
                .andExpect(jsonPath("$.data.finishedAt[4]").value(5));
    }

    @Test
    @DisplayName("重试失败任务应返回新任务信息")
    void retrySolverTask_shouldReturnRetriedTask() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID failedTaskId = UUID.randomUUID();
        UUID retriedTaskId = UUID.randomUUID();
        ScheduleSolverTask task = buildTask(retriedTaskId, scheduleId, SolverTaskStatus.PENDING);
        when(scheduleService.retryFailedTask(failedTaskId)).thenReturn(task);

        mockMvc.perform(post("/api/schedules/solver-tasks/{taskId}/retry", failedTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(retriedTaskId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    private ScheduleSolverTask buildTask(UUID taskId, UUID scheduleId, SolverTaskStatus status) {
        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(taskId);
        task.setScheduleId(scheduleId);
        task.setTaskType(SolverTaskType.PLAN);
        task.setTriggerSource(TriggerSource.MANUAL);
        task.setStatus(status);
        return task;
    }
}
