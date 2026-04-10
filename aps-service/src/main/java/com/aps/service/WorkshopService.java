package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.entity.Workshop;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.FactoryCalendarRepository;
import com.aps.service.repository.ResourceRepository;
import com.aps.service.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final FactoryCalendarRepository calendarRepository;
    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public List<Workshop> getAllWorkshops() {
        return workshopRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public Workshop getWorkshopById(UUID id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("车间不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, resource = "Workshop")
    public Workshop createWorkshop(String code, String name, UUID calendarId,
                                   String managerName, Integer sortOrder, String description) {
        if (workshopRepository.existsByCode(code)) {
            throw new ResourceConflictException("车间编码已存在: " + code);
        }
        Workshop workshop = new Workshop();
        workshop.setCode(code);
        workshop.setName(name);
        workshop.setManagerName(managerName);
        workshop.setSortOrder(sortOrder != null ? sortOrder : 0);
        workshop.setDescription(description);
        workshop.setEnabled(true);

        if (calendarId != null) {
            FactoryCalendar calendar = calendarRepository.findById(calendarId)
                    .orElseThrow(() -> new ResourceNotFoundException("日历不存在: " + calendarId));
            workshop.setCalendar(calendar);
        }

        return workshopRepository.save(workshop);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "Workshop")
    public Workshop updateWorkshop(UUID id, String name, UUID calendarId,
                                   String managerName, Integer sortOrder,
                                   String description, Boolean enabled) {
        return updateWorkshop(id, name, calendarId, managerName, sortOrder, description, enabled, true);
    }

    @Transactional
    public Workshop updateWorkshop(UUID id, String name, UUID calendarId,
                                   String managerName, Integer sortOrder,
                                   String description, Boolean enabled,
                                   boolean calendarIdProvided) {
        Workshop workshop = getWorkshopById(id);
        if (name != null) workshop.setName(name);
        if (managerName != null) workshop.setManagerName(managerName);
        if (sortOrder != null) workshop.setSortOrder(sortOrder);
        if (description != null) workshop.setDescription(description);
        if (enabled != null) workshop.setEnabled(enabled);

        if (calendarIdProvided) {
            if (calendarId == null) {
                workshop.setCalendar(null);
            } else {
                FactoryCalendar calendar = calendarRepository.findById(calendarId)
                        .orElseThrow(() -> new ResourceNotFoundException("日历不存在: " + calendarId));
                workshop.setCalendar(calendar);
            }
        }

        return workshopRepository.save(workshop);
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, resource = "Workshop")
    public void deleteWorkshop(UUID id) {
        if (!workshopRepository.existsById(id)) {
            throw new ResourceNotFoundException("车间不存在: " + id);
        }
        if (resourceRepository.existsByWorkshopId(id)) {
            throw new ResourceConflictException("该车间下存在注塑机，无法删除");
        }
        workshopRepository.deleteById(id);
    }

    /**
     * 获取车间有效日历（日历继承：车间日历 → 全局默认日历）
     */
    @Transactional(readOnly = true)
    public FactoryCalendar getEffectiveCalendar(UUID workshopId) {
        Workshop workshop = getWorkshopById(workshopId);
        if (workshop.getCalendar() != null) {
            return workshop.getCalendar();
        }
        return calendarRepository.findByIsDefaultTrue().orElse(null);
    }
}
