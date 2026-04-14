package com.aps.api.dto;

import jakarta.validation.constraints.Email;

import java.util.List;
import java.util.UUID;

public record UpdateUserRequest(
    @Email(message = "邮箱格式不正确")
    String email,

    Boolean enabled,

    List<UUID> roleIds
) {
}
