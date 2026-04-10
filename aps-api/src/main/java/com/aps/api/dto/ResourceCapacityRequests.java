package com.aps.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ResourceCapacityRequests {

    private ResourceCapacityRequests() {
    }

    public record ResourceCapacityUpdateRequest(
            @PositiveOrZero(message = "班次分钟数不能小于0")
            Integer shiftMinutesOverride,
            @NotNull(message = "利用率不能为空")
            @DecimalMin(value = "0.0", message = "利用率不能小于0")
            @DecimalMax(value = "1.0", message = "利用率不能大于1")
            BigDecimal utilizationRate,
            String remark
    ) {
    }

    public record ResourceCapacityBatchUpdateRequest(
            @NotEmpty(message = "日期列表不能为空")
            List<@NotNull(message = "日期不能为空") LocalDate> dates,
            @PositiveOrZero(message = "班次分钟数不能小于0")
            Integer shiftMinutesOverride,
            @NotNull(message = "利用率不能为空")
            @DecimalMin(value = "0.0", message = "利用率不能小于0")
            @DecimalMax(value = "1.0", message = "利用率不能大于1")
            BigDecimal utilizationRate,
            String remark
    ) {
    }
}
