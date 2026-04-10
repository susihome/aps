package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.FactoryCalendar;
import com.aps.domain.entity.Resource;
import com.aps.domain.entity.Workshop;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.MachineStatus;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.FactoryCalendarRepository;
import com.aps.service.repository.ResourceRepository;
import com.aps.service.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final WorkshopRepository workshopRepository;
    private final FactoryCalendarRepository calendarRepository;

    @Transactional(readOnly = true)
    public List<Resource> getAllResources(UUID workshopId, MachineStatus status) {
        if (workshopId != null && status != null) {
            return resourceRepository.findByWorkshopIdAndStatus(workshopId, status);
        }
        if (workshopId != null) {
            return resourceRepository.findByWorkshopId(workshopId);
        }
        if (status != null) {
            return resourceRepository.findByStatus(status);
        }
        return resourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Resource getResourceById(UUID id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.RESOURCE_CREATE, resource = "Resource")
    public Resource createResource(String resourceCode, String resourceName, String resourceType,
                                   UUID workshopId, Integer tonnage, String machineBrand,
                                   String machineModel, BigDecimal maxShotWeight,
                                   MachineStatus status, UUID calendarId) {
        resourceRepository.findByResourceCode(resourceCode).ifPresent(r -> {
            throw new ResourceConflictException("资源编码已存在: " + resourceCode);
        });

        Resource resource = new Resource();
        resource.setResourceCode(resourceCode);
        resource.setResourceName(resourceName);
        resource.setResourceType(resourceType);
        resource.setAvailable(true);
        resource.setTonnage(tonnage);
        resource.setMachineBrand(machineBrand);
        resource.setMachineModel(machineModel);
        resource.setMaxShotWeight(maxShotWeight);
        resource.setStatus(status);

        if (workshopId != null) {
            Workshop workshop = workshopRepository.findById(workshopId)
                    .orElseThrow(() -> new ResourceNotFoundException("车间不存在: " + workshopId));
            resource.setWorkshop(workshop);
        }

        if (calendarId != null) {
            FactoryCalendar calendar = calendarRepository.findById(calendarId)
                    .orElseThrow(() -> new ResourceNotFoundException("日历不存在: " + calendarId));
            resource.setCalendar(calendar);
        }

        return resourceRepository.save(resource);
    }

    @Transactional
    @Audited(action = AuditAction.RESOURCE_UPDATE, resource = "Resource")
    public Resource updateResource(UUID id, String resourceName, String resourceType,
                                   UUID workshopId, Integer tonnage, String machineBrand,
                                   String machineModel, BigDecimal maxShotWeight,
                                   MachineStatus status, UUID calendarId, Boolean available) {
        return updateResource(id, resourceName, resourceType, workshopId, tonnage, machineBrand,
                machineModel, maxShotWeight, status, calendarId, available, true, true);
    }

    @Transactional
    public Resource updateResource(UUID id, String resourceName, String resourceType,
                                   UUID workshopId, Integer tonnage, String machineBrand,
                                   String machineModel, BigDecimal maxShotWeight,
                                   MachineStatus status, UUID calendarId, Boolean available,
                                   boolean workshopIdProvided, boolean calendarIdProvided) {
        Resource resource = getResourceById(id);
        if (resourceName != null) resource.setResourceName(resourceName);
        if (resourceType != null) resource.setResourceType(resourceType);
        if (tonnage != null) resource.setTonnage(tonnage);
        if (machineBrand != null) resource.setMachineBrand(machineBrand);
        if (machineModel != null) resource.setMachineModel(machineModel);
        if (maxShotWeight != null) resource.setMaxShotWeight(maxShotWeight);
        if (status != null) resource.setStatus(status);
        if (available != null) resource.setAvailable(available);

        if (workshopIdProvided) {
            if (workshopId == null) {
                resource.setWorkshop(null);
            } else {
                Workshop workshop = workshopRepository.findById(workshopId)
                        .orElseThrow(() -> new ResourceNotFoundException("车间不存在: " + workshopId));
                resource.setWorkshop(workshop);
            }
        }

        if (calendarIdProvided) {
            if (calendarId == null) {
                resource.setCalendar(null);
            } else {
                FactoryCalendar calendar = calendarRepository.findById(calendarId)
                        .orElseThrow(() -> new ResourceNotFoundException("日历不存在: " + calendarId));
                resource.setCalendar(calendar);
            }
        }

        return resourceRepository.save(resource);
    }

    @Transactional
    @Audited(action = AuditAction.RESOURCE_DELETE, resource = "Resource")
    public void deleteResource(UUID id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("资源不存在: " + id);
        }
        resourceRepository.deleteById(id);
    }

    /**
     * 获取资源的有效日历（三级继承：资源 → 车间 → 全局默认）
     */
    @Transactional(readOnly = true)
    public FactoryCalendar getEffectiveCalendar(UUID resourceId) {
        Resource resource = getResourceById(resourceId);
        if (resource.getCalendar() != null) {
            return resource.getCalendar();
        }
        if (resource.getWorkshop() != null && resource.getWorkshop().getCalendar() != null) {
            return resource.getWorkshop().getCalendar();
        }
        return calendarRepository.findByIsDefaultTrue().orElse(null);
    }
}
