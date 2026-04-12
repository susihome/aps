package com.aps.api.dto;

import com.aps.domain.entity.Mold;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldDto {

    private UUID id;
    private String moldCode;
    private String moldName;
    private Integer cavityCount;
    private String status;
    private Boolean enabled;
    private String remark;
    private Integer requiredTonnage;
    private BigDecimal maxShotWeight;
    private String maintenanceState;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MoldDto fromEntity(Mold mold) {
        return MoldDto.builder()
                .id(mold.getId())
                .moldCode(mold.getMoldCode())
                .moldName(mold.getMoldName())
                .cavityCount(mold.getCavityCount())
                .status(mold.getStatus())
                .enabled(mold.getEnabled())
                .remark(mold.getRemark())
                .requiredTonnage(mold.getRequiredTonnage())
                .maxShotWeight(mold.getMaxShotWeight())
                .maintenanceState(mold.getMaintenanceState())
                .createTime(mold.getCreateTime())
                .updateTime(mold.getUpdateTime())
                .build();
    }
}
