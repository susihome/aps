package com.aps.api.dto;

import com.aps.domain.entity.ResourceCapacityDay;
import com.aps.domain.enums.DateType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class ResourceCapacityDayDto {
    LocalDate date;
    DateType dateType;
    String dateLabel;
    Integer defaultShiftMinutes;
    Integer shiftMinutesOverride;
    Integer effectiveShiftMinutes;
    BigDecimal utilizationRate;
    Integer availableCapacityMinutes;
    String remark;
    boolean overridden;

    public static ResourceCapacityDayDto of(
            LocalDate date,
            DateType dateType,
            String dateLabel,
            Integer defaultShiftMinutes,
            ResourceCapacityDay day,
            Integer effectiveShiftMinutes,
            Integer availableCapacityMinutes
    ) {
        return ResourceCapacityDayDto.builder()
                .date(date)
                .dateType(dateType)
                .dateLabel(dateLabel)
                .defaultShiftMinutes(defaultShiftMinutes)
                .shiftMinutesOverride(day != null ? day.getShiftMinutesOverride() : null)
                .effectiveShiftMinutes(effectiveShiftMinutes)
                .utilizationRate(day != null ? day.getUtilizationRate() : BigDecimal.ONE)
                .availableCapacityMinutes(availableCapacityMinutes)
                .remark(day != null ? day.getRemark() : null)
                .overridden(day != null && day.getShiftMinutesOverride() != null)
                .build();
    }
}
