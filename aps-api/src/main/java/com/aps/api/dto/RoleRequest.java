package com.aps.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Size(max = 255, message = "描述长度不能超过 255 个字符")
    private String description;

    private List<UUID> permissionIds;
}
