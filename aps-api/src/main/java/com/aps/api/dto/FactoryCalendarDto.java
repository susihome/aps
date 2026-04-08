package com.aps.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactoryCalendarDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Integer year;
    private Boolean isDefault;
    private Boolean enabled;
    private List<CalendarShiftDto> shifts;
    private Long workdayCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static FactoryCalendarDto fromEntity(com.aps.domain.entity.FactoryCalendar cal, long workdayCount) {
        return FactoryCalendarDto.builder()
                .id(cal.getId())
                .name(cal.getName())
                .code(cal.getCode())
                .description(cal.getDescription())
                .year(cal.getYear())
                .isDefault(cal.getIsDefault())
                .enabled(cal.getEnabled())
                .shifts(cal.getShifts() != null ? cal.getShifts().stream().map(CalendarShiftDto::fromEntity).toList() : List.of())
                .workdayCount(workdayCount)
                .createTime(cal.getCreateTime())
                .updateTime(cal.getUpdateTime())
                .build();
    }

    public static FactoryCalendarDto fromEntity(com.aps.domain.entity.FactoryCalendar cal) {
        return fromEntity(cal, 0L);
    }
}
