package com.aps.api.dto;

import com.aps.domain.enums.DateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDateDto {
    private UUID id;
    private LocalDate date;
    private DateType dateType;
    private String label;

    public static CalendarDateDto fromEntity(com.aps.domain.entity.CalendarDate cd) {
        return CalendarDateDto.builder()
                .id(cd.getId())
                .date(cd.getDate())
                .dateType(cd.getDateType())
                .label(cd.getLabel())
                .build();
    }
}
