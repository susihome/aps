package com.aps.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DictTypeRequest(
        @NotBlank(message = "字典类型编码不能为空")
        @Size(max = 64, message = "字典类型编码长度不能超过64")
        String code,

        @NotBlank(message = "字典类型名称不能为空")
        @Size(max = 100, message = "字典类型名称长度不能超过100")
        String name,

        @Size(max = 500, message = "描述长度不能超过500")
        String description,

        Boolean enabled,
        Integer sortOrder
) {
}
