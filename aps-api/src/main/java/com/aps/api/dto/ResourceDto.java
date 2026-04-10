package com.aps.api.dto;

import com.aps.domain.entity.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDto {

    private UUID id;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private Boolean available;

    private UUID workshopId;
    private String workshopName;
    private Integer tonnage;
    private String machineBrand;
    private String machineModel;
    private BigDecimal maxShotWeight;
    private String status;
    private UUID calendarId;
    private String calendarName;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ResourceDto fromEntity(Resource resource) {
        boolean workshopInitialized = Hibernate.isInitialized(resource.getWorkshop()) && resource.getWorkshop() != null;
        boolean calendarInitialized = Hibernate.isInitialized(resource.getCalendar()) && resource.getCalendar() != null;

        return ResourceDto.builder()
                .id(resource.getId())
                .resourceCode(resource.getResourceCode())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType())
                .available(resource.getAvailable())
                .workshopId(workshopInitialized ? resource.getWorkshop().getId() : null)
                .workshopName(workshopInitialized ? resource.getWorkshop().getName() : null)
                .tonnage(resource.getTonnage())
                .machineBrand(resource.getMachineBrand())
                .machineModel(resource.getMachineModel())
                .maxShotWeight(resource.getMaxShotWeight())
                .status(resource.getStatus() != null ? resource.getStatus().name() : null)
                .calendarId(calendarInitialized ? resource.getCalendar().getId() : null)
                .calendarName(calendarInitialized ? resource.getCalendar().getName() : null)
                .createTime(resource.getCreateTime())
                .updateTime(resource.getUpdateTime())
                .build();
    }
}
