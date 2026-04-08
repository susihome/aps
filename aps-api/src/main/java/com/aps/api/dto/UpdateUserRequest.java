package com.aps.api.dto;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    @Email(message = "邮箱格式不正确")
    String email,

    Boolean enabled
) {
}
