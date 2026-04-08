package com.aps.api.controller;

import com.aps.api.annotation.Audited;
import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.CreateUserRequest;
import com.aps.api.dto.UpdateUserRequest;
import com.aps.api.dto.UserDto;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.RoleType;
import com.aps.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户（仅管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return AjaxResult.success(userDtos);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public AjaxResult<UserDto> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 创建用户（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = AuditAction.CREATE, resource = "user")
    public AjaxResult<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.username(),
                request.password(),
                request.email()
        );
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 更新用户（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = AuditAction.UPDATE, resource = "user")
    public AjaxResult<UserDto> updateUser(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.email(), request.enabled());
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = AuditAction.DELETE, resource = "user")
    public AjaxResult<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return AjaxResult.success(null);
    }

    /**
     * 为用户分配角色（仅管理员）
     */
    @PostMapping("/{id}/roles/{roleType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = AuditAction.UPDATE, resource = "user_role")
    public AjaxResult<Void> assignRole(@PathVariable UUID id,
                                       @PathVariable RoleType roleType) {
        userService.assignRole(id, roleType);
        return AjaxResult.success(null);
    }

    /**
     * 移除用户角色（仅管理员）
     */
    @DeleteMapping("/{id}/roles/{roleType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = AuditAction.UPDATE, resource = "user_role")
    public AjaxResult<Void> removeRole(@PathVariable UUID id,
                                       @PathVariable RoleType roleType) {
        userService.removeRole(id, roleType);
        return AjaxResult.success(null);
    }
}
