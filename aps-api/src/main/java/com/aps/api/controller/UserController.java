package com.aps.api.controller;

import com.aps.api.annotation.Audited;
import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.CreateUserRequest;
import com.aps.api.dto.ResetUserPasswordRequest;
import com.aps.api.dto.UpdateUserRequest;
import com.aps.api.dto.UserDto;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.RoleType;
import com.aps.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户（仅管理员）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public AjaxResult<Page<UserDto>> getAllUsers(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Boolean enabled,
                                                 @PageableDefault(size = 20, sort = {"createTime", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> users = userService.getUsers(keyword, enabled, pageable);
        return AjaxResult.success(users.map(UserDto::fromEntity));
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list') or #id == authentication.principal.id")
    public AjaxResult<UserDto> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 创建用户（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @Audited(action = AuditAction.CREATE, resource = "user")
    public AjaxResult<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.username(),
                request.password(),
                request.email(),
                request.roleIds()
        );
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 更新用户（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Audited(action = AuditAction.UPDATE, resource = "user")
    public AjaxResult<UserDto> updateUser(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request.email(), request.enabled(), request.roleIds());
        UserDto userDto = UserDto.fromEntity(user);
        return AjaxResult.success(userDto);
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('system:user:delete', 'system:user:remove')")
    @Audited(action = AuditAction.DELETE, resource = "user")
    public AjaxResult<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return AjaxResult.success(null);
    }

    /**
     * 重置用户密码（仅管理员）
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:reset_password')")
    @Audited(action = AuditAction.UPDATE, resource = "user_password")
    public AjaxResult<Void> resetPassword(@PathVariable UUID id,
                                          @Valid @RequestBody ResetUserPasswordRequest request) {
        userService.resetPassword(id, request.newPassword());
        return AjaxResult.success(null);
    }

    /**
     * 为用户分配角色（仅管理员）
     */
    @PostMapping("/{id}/roles/{roleType}")
    @PreAuthorize("hasAuthority('system:user:edit')")
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
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Audited(action = AuditAction.UPDATE, resource = "user_role")
    public AjaxResult<Void> removeRole(@PathVariable UUID id,
                                       @PathVariable RoleType roleType) {
        userService.removeRole(id, roleType);
        return AjaxResult.success(null);
    }
}
