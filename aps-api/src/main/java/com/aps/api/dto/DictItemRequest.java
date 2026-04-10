package com.aps.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DictItemRequest(
        @NotBlank(message = "字典项编码不能为空")
        @Size(max = 64, message = "字典项编码长度不能超过64")
        String itemCode,

        @NotBlank(message = "字典项名称不能为空")
        @Size(max = 100, message = "字典项名称长度不能超过100")
        String itemName,

        @NotBlank(message = "字典项值不能为空")
        @Size(max = 100, message = "字典项值长度不能超过100")
        String itemValue,

        @Size(max = 500, message = "描述长度不能超过500")
        String description,

        Boolean enabled,
        Integer sortOrder,
        Boolean isSystem
) {
}
