package com.aps.api.dto;

import com.aps.domain.entity.Workshop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopDto {

    private UUID id;
    private String code;
    private String name;
    private UUID calendarId;
    private String calendarName;
    private String managerName;
    private Boolean enabled;
    private Integer sortOrder;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static WorkshopDto fromEntity(Workshop workshop) {
        boolean calendarInitialized = Hibernate.isInitialized(workshop.getCalendar()) && workshop.getCalendar() != null;
        return WorkshopDto.builder()
                .id(workshop.getId())
                .code(workshop.getCode())
                .name(workshop.getName())
                .calendarId(calendarInitialized ? workshop.getCalendar().getId() : null)
                .calendarName(calendarInitialized ? workshop.getCalendar().getName() : null)
                .managerName(workshop.getManagerName())
                .enabled(workshop.getEnabled())
                .sortOrder(workshop.getSortOrder())
                .description(workshop.getDescription())
                .createTime(workshop.getCreateTime())
                .updateTime(workshop.getUpdateTime())
                .build();
    }
}
