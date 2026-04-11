package com.aps.api.dto;

import com.aps.domain.entity.Material;
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
public class MaterialDto {

    private UUID id;
    private String materialCode;
    private String materialName;
    private String specification;
    private String unit;
    private Boolean enabled;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 车间排产属性
    private String colorCode;
    private String rawMaterialType;
    private Integer defaultLotSize;
    private Integer minLotSize;
    private Integer maxLotSize;
    private Boolean allowDelay;
    private String abcClassification;
    private String productGroup;

    public static MaterialDto fromEntity(Material material) {
        return MaterialDto.builder()
                .id(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(material.getMaterialName())
                .specification(material.getSpecification())
                .unit(material.getUnit())
                .enabled(material.getEnabled())
                .remark(material.getRemark())
                .createTime(material.getCreateTime())
                .updateTime(material.getUpdateTime())
                .colorCode(material.getColorCode())
                .rawMaterialType(material.getRawMaterialType())
                .defaultLotSize(material.getDefaultLotSize())
                .minLotSize(material.getMinLotSize())
                .maxLotSize(material.getMaxLotSize())
                .allowDelay(material.getAllowDelay())
                .abcClassification(material.getAbcClassification())
                .productGroup(material.getProductGroup())
                .build();
    }
}
