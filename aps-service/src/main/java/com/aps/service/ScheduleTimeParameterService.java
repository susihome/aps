package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Resource;
import com.aps.domain.entity.ScheduleTimeParameter;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.ResourceRepository;
import com.aps.service.repository.ScheduleTimeParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleTimeParameterService {

    private final ScheduleTimeParameterRepository repo;
    private final ResourceRepository resourceRepository;

    // ===== 值对象 =====

    public record OrderFilterWindow(LocalDateTime start, LocalDateTime end) {}

    public record DisplayWindow(LocalDate start, LocalDate end) {}

    // ===== CRUD =====

    @Transactional(readOnly = true)
    public List<ScheduleTimeParameter> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public ScheduleTimeParameter findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("排程时间参数不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.SCHEDULE_TIME_PARAM_CREATE, resource = "ScheduleTimeParameter")
    public ScheduleTimeParameter create(
            UUID resourceId,
            Integer orderFilterStartDays, LocalTime orderFilterStartTime,
            Integer orderFilterEndDays, LocalTime orderFilterEndTime,
            Integer planningStartDays, LocalTime planningStartTime,
            Integer displayStartDays, Integer displayEndDays,
            Integer completionDays, Integer timeScale, Integer factor, Integer exceedPeriod,
            Boolean isDefault, Boolean enabled, String remark) {

        validateParams(
                orderFilterStartDays != null ? orderFilterStartDays : 0,
                orderFilterEndDays != null ? orderFilterEndDays : 14,
                displayStartDays != null ? displayStartDays : 0,
                displayEndDays != null ? displayEndDays : 30,
                timeScale != null ? timeScale : 1,
                exceedPeriod);

        if (resourceId != null) {
            if (repo.existsByResource_Id(resourceId)) {
                throw new ResourceConflictException("该设备已存在排程时间参数配置");
            }
        } else {
            if (Boolean.TRUE.equals(isDefault) && repo.existsByIsDefaultTrue()) {
                throw new ResourceConflictException("已存在全局默认配置，不能重复创建");
            }
        }

        ScheduleTimeParameter param = new ScheduleTimeParameter();
        if (resourceId != null) {
            Resource resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("设备不存在: " + resourceId));
            param.setResource(resource);
        }
        applyFields(param, orderFilterStartDays, orderFilterStartTime,
                orderFilterEndDays, orderFilterEndTime,
                planningStartDays, planningStartTime,
                displayStartDays, displayEndDays,
                completionDays, timeScale, factor, exceedPeriod,
                isDefault, enabled, remark);
        return repo.save(param);
    }

    @Transactional
    @Audited(action = AuditAction.SCHEDULE_TIME_PARAM_UPDATE, resource = "ScheduleTimeParameter")
    public ScheduleTimeParameter update(
            UUID id,
            Integer orderFilterStartDays, LocalTime orderFilterStartTime,
            Integer orderFilterEndDays, LocalTime orderFilterEndTime,
            Integer planningStartDays, LocalTime planningStartTime,
            Integer displayStartDays, Integer displayEndDays,
            Integer completionDays, Integer timeScale, Integer factor, Integer exceedPeriod,
            Boolean isDefault, Boolean enabled, String remark) {

        ScheduleTimeParameter param = findById(id);
        validateParams(
                orderFilterStartDays != null ? orderFilterStartDays : param.getOrderFilterStartDays(),
                orderFilterEndDays != null ? orderFilterEndDays : param.getOrderFilterEndDays(),
                displayStartDays != null ? displayStartDays : param.getDisplayStartDays(),
                displayEndDays != null ? displayEndDays : param.getDisplayEndDays(),
                timeScale != null ? timeScale : param.getTimeScale(),
                exceedPeriod);

        if (Boolean.TRUE.equals(isDefault) && !Boolean.TRUE.equals(param.getIsDefault())) {
            repo.findByIsDefaultTrue().ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ResourceConflictException("已存在全局默认配置，请先取消其他默认配置");
                }
            });
        }

        if (orderFilterStartDays != null) param.setOrderFilterStartDays(orderFilterStartDays);
        if (orderFilterStartTime != null) param.setOrderFilterStartTime(orderFilterStartTime);
        if (orderFilterEndDays != null) param.setOrderFilterEndDays(orderFilterEndDays);
        if (orderFilterEndTime != null) param.setOrderFilterEndTime(orderFilterEndTime);
        if (planningStartDays != null) param.setPlanningStartDays(planningStartDays);
        if (planningStartTime != null) param.setPlanningStartTime(planningStartTime);
        if (displayStartDays != null) param.setDisplayStartDays(displayStartDays);
        if (displayEndDays != null) param.setDisplayEndDays(displayEndDays);
        if (completionDays != null) param.setCompletionDays(completionDays);
        if (timeScale != null) param.setTimeScale(timeScale);
        if (factor != null) param.setFactor(factor);
        // exceedPeriod 是 nullable 字段，使用独立标记处理"清空"场景
        // 这里只在显式传值时更新，前端需要传 0 来表示"无超出期间"
        if (exceedPeriod != null) param.setExceedPeriod(exceedPeriod);
        if (isDefault != null) param.setIsDefault(isDefault);
        if (enabled != null) param.setEnabled(enabled);
        if (remark != null) param.setRemark(remark);
        return repo.save(param);
    }

    @Transactional
    @Audited(action = AuditAction.SCHEDULE_TIME_PARAM_DELETE, resource = "ScheduleTimeParameter")
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("排程时间参数不存在: " + id);
        }
        repo.deleteById(id);
    }

    // ===== 核心查询方法 =====

    /**
     * 获取工单筛选范围 — "捞哪些工单"
     * 交期落在此范围内的工单才会被排程引擎处理
     */
    @Transactional(readOnly = true)
    public OrderFilterWindow getOrderFilterWindow(UUID resourceId) {
        ScheduleTimeParameter p = findEffectiveParam(resourceId);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.plusDays(p.getOrderFilterStartDays())
                                   .atTime(p.getOrderFilterStartTime());
        LocalDateTime end = today.plusDays(p.getOrderFilterEndDays())
                                 .atTime(p.getOrderFilterEndTime());
        return new OrderFilterWindow(start, end);
    }

    /**
     * 获取排程安排起点 — "从什么时候开始排"（同时也是冻结期边界）
     * Today → planningStart 之间的既有 Assignment 应标记为 pinned
     */
    @Transactional(readOnly = true)
    public LocalDateTime getPlanningStart(UUID resourceId) {
        ScheduleTimeParameter p = findEffectiveParam(resourceId);
        LocalDate today = LocalDate.now();
        return today.plusDays(p.getPlanningStartDays())
                    .atTime(p.getPlanningStartTime());
    }

    /**
     * 获取显示窗口 — "看多远"（甘特图用）
     */
    @Transactional(readOnly = true)
    public DisplayWindow getDisplayWindow(UUID resourceId) {
        ScheduleTimeParameter p = findEffectiveParam(resourceId);
        LocalDate today = LocalDate.now();
        return new DisplayWindow(
                today.plusDays(p.getDisplayStartDays()),
                today.plusDays(p.getDisplayEndDays())
        );
    }

    /**
     * 构建预览结果（一次查询，避免重复 DB 访问）
     */
    @Transactional(readOnly = true)
    public PreviewResult buildPreview(UUID resourceId) {
        ScheduleTimeParameter p = findEffectiveParam(resourceId);
        LocalDate today = LocalDate.now();
        OrderFilterWindow orderWindow = new OrderFilterWindow(
                today.plusDays(p.getOrderFilterStartDays()).atTime(p.getOrderFilterStartTime()),
                today.plusDays(p.getOrderFilterEndDays()).atTime(p.getOrderFilterEndTime()));
        LocalDateTime planningStart = today.plusDays(p.getPlanningStartDays()).atTime(p.getPlanningStartTime());
        DisplayWindow displayWindow = new DisplayWindow(
                today.plusDays(p.getDisplayStartDays()),
                today.plusDays(p.getDisplayEndDays()));
        return new PreviewResult(p, orderWindow, planningStart, displayWindow);
    }

    public record PreviewResult(
            ScheduleTimeParameter param,
            OrderFilterWindow orderFilterWindow,
            LocalDateTime planningStart,
            DisplayWindow displayWindow) {}

    /**
     * 获取完成期限上限（天）= completionDays + exceedPeriod
     */
    @Transactional(readOnly = true)
    public int getEffectiveCompletionDays(UUID resourceId) {
        ScheduleTimeParameter p = findEffectiveParam(resourceId);
        int exceed = p.getExceedPeriod() != null ? p.getExceedPeriod() : 0;
        return p.getCompletionDays() + exceed;
    }

    /**
     * 查找有效配置：Resource级 → 全局默认(isDefault=true) → 系统兜底默认值
     */
    public ScheduleTimeParameter findEffectiveParam(UUID resourceId) {
        if (resourceId != null) {
            Optional<ScheduleTimeParameter> specific =
                    repo.findByResource_IdAndEnabledTrue(resourceId);
            if (specific.isPresent()) return specific.get();
        }
        return repo.findByIsDefaultTrueAndEnabledTrue()
                   .orElse(systemDefault());
    }

    // ===== 私有方法 =====

    private ScheduleTimeParameter systemDefault() {
        ScheduleTimeParameter d = new ScheduleTimeParameter();
        LocalDateTime now = LocalDateTime.now();
        d.setOrderFilterStartDays(0);
        d.setOrderFilterStartTime(LocalTime.of(8, 0));
        d.setOrderFilterEndDays(14);
        d.setOrderFilterEndTime(LocalTime.of(0, 0));
        d.setPlanningStartDays(0);
        d.setPlanningStartTime(LocalTime.of(9, 0));
        d.setDisplayStartDays(0);
        d.setDisplayEndDays(30);
        d.setCompletionDays(0);
        d.setTimeScale(1);
        d.setFactor(0);
        d.setExceedPeriod(null);
        d.setIsDefault(true);
        d.setEnabled(true);
        d.setCreateTime(now);
        d.setUpdateTime(now);
        return d;
    }

    private void applyFields(ScheduleTimeParameter param,
                             Integer orderFilterStartDays, LocalTime orderFilterStartTime,
                             Integer orderFilterEndDays, LocalTime orderFilterEndTime,
                             Integer planningStartDays, LocalTime planningStartTime,
                             Integer displayStartDays, Integer displayEndDays,
                             Integer completionDays, Integer timeScale, Integer factor, Integer exceedPeriod,
                             Boolean isDefault, Boolean enabled, String remark) {
        param.setOrderFilterStartDays(orderFilterStartDays != null ? orderFilterStartDays : 0);
        param.setOrderFilterStartTime(orderFilterStartTime != null ? orderFilterStartTime : LocalTime.of(8, 0));
        param.setOrderFilterEndDays(orderFilterEndDays != null ? orderFilterEndDays : 14);
        param.setOrderFilterEndTime(orderFilterEndTime != null ? orderFilterEndTime : LocalTime.of(0, 0));
        param.setPlanningStartDays(planningStartDays != null ? planningStartDays : 0);
        param.setPlanningStartTime(planningStartTime != null ? planningStartTime : LocalTime.of(9, 0));
        param.setDisplayStartDays(displayStartDays != null ? displayStartDays : 0);
        param.setDisplayEndDays(displayEndDays != null ? displayEndDays : 30);
        param.setCompletionDays(completionDays != null ? completionDays : 0);
        param.setTimeScale(timeScale != null ? timeScale : 1);
        param.setFactor(factor != null ? factor : 0);
        param.setExceedPeriod(exceedPeriod);
        param.setIsDefault(Boolean.TRUE.equals(isDefault));
        param.setEnabled(enabled == null || enabled);
        param.setRemark(remark);
    }

    private void validateParams(int orderFilterStartDays, int orderFilterEndDays,
                                int displayStartDays, int displayEndDays,
                                int timeScale, Integer exceedPeriod) {
        if (orderFilterStartDays < 0) throw new IllegalArgumentException("工单筛选起始天数不能为负数");
        if (orderFilterEndDays < orderFilterStartDays) throw new IllegalArgumentException("工单筛选终止天数不能早于起始天数");
        if (displayEndDays <= displayStartDays) throw new IllegalArgumentException("显示结束天数必须大于显示起始天数");
        if (timeScale < 1) throw new IllegalArgumentException("时间刻度不能小于1");
        if (exceedPeriod != null && exceedPeriod < 0) throw new IllegalArgumentException("超出期间不能为负数");
    }
}
