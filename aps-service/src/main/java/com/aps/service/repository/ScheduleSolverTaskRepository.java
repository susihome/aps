package com.aps.service.repository;

import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.SolverTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleSolverTaskRepository extends JpaRepository<ScheduleSolverTask, UUID> {

    Optional<ScheduleSolverTask> findFirstByScheduleIdOrderByCreateTimeDesc(UUID scheduleId);

    List<ScheduleSolverTask> findTop10ByScheduleIdOrderByCreateTimeDesc(UUID scheduleId);

    Optional<ScheduleSolverTask> findFirstByScheduleIdAndStatusOrderByCreateTimeDesc(
            UUID scheduleId,
            SolverTaskStatus status);

    List<ScheduleSolverTask> findTop100ByStatusOrderByCreateTimeAsc(SolverTaskStatus status);

    List<ScheduleSolverTask> findTop100ByStatusAndCreateTimeBeforeOrderByCreateTimeAsc(
            SolverTaskStatus status,
            LocalDateTime createTimeBefore);
}
