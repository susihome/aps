package com.aps.api.dto;

import com.aps.domain.entity.MaterialMoldBinding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMoldBindingDto {

    private UUID id;
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private UUID moldId;
    private String moldCode;
    private String moldName;
    private Integer priority;
    private Boolean isDefault;
    private Boolean isPreferred;
    private Integer cycleTimeMinutes;
    private Integer setupTimeMinutes;
    private Integer changeoverTimeMinutes;
    private Boolean enabled;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MaterialMoldBindingDto fromEntity(MaterialMoldBinding binding) {
        return MaterialMoldBindingDto.builder()
                .id(binding.getId())
                .materialId(binding.getMaterial().getId())
                .materialCode(binding.getMaterial().getMaterialCode())
                .materialName(binding.getMaterial().getMaterialName())
                .moldId(binding.getMold().getId())
                .moldCode(binding.getMold().getMoldCode())
                .moldName(binding.getMold().getMoldName())
                .priority(binding.getPriority())
                .isDefault(binding.getIsDefault())
                .isPreferred(binding.getIsPreferred())
                .cycleTimeMinutes(binding.getCycleTimeMinutes())
                .setupTimeMinutes(binding.getSetupTimeMinutes())
                .changeoverTimeMinutes(binding.getChangeoverTimeMinutes())
                .enabled(binding.getEnabled())
                .validFrom(binding.getValidFrom())
                .validTo(binding.getValidTo())
                .remark(binding.getRemark())
                .createTime(binding.getCreateTime())
                .updateTime(binding.getUpdateTime())
                .build();
    }
}
