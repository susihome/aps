package com.aps.api.dto;

import com.aps.domain.entity.DictType;
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
public class DictTypeDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static DictTypeDto fromEntity(DictType entity) {
        return DictTypeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .sortOrder(entity.getSortOrder())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
