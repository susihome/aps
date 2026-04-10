package com.aps.api.dto;

import com.aps.domain.entity.DictItem;
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
public class DictItemDto {

    private UUID id;
    private UUID dictTypeId;
    private String dictTypeCode;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private String description;
    private Boolean enabled;
    private Integer sortOrder;
    private Boolean isSystem;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static DictItemDto fromEntity(DictItem entity) {
        return DictItemDto.builder()
                .id(entity.getId())
                .dictTypeId(entity.getDictType() != null ? entity.getDictType().getId() : null)
                .dictTypeCode(entity.getDictType() != null ? entity.getDictType().getCode() : null)
                .itemCode(entity.getItemCode())
                .itemName(entity.getItemName())
                .itemValue(entity.getItemValue())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .sortOrder(entity.getSortOrder())
                .isSystem(entity.getIsSystem())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
