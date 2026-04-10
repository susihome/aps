package com.aps.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarShiftDto {
    private UUID id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer sortOrder;
    private Integer breakMinutes;
    private Boolean nextDay;

    public static CalendarShiftDto fromEntity(com.aps.domain.entity.CalendarShift shift) {
        return CalendarShiftDto.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .sortOrder(shift.getSortOrder())
                .breakMinutes(shift.getBreakMinutes())
                .nextDay(shift.getNextDay())
                .build();
    }
}
