package com.aps.service;

import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.repository.RoleRepository;
import com.aps.service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthSessionService authSessionService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("创建用户时应规范化邮箱并分配角色")
    void createUser_shouldNormalizeEmailAndAssignRoles() {
        UUID roleId = UUID.randomUUID();
        Role role = buildRole(roleId, "ADMIN");
        User saved = new User();
        UUID userId = UUID.randomUUID();
        saved.setId(userId);
        saved.setUsername("planner");
        saved.setEmail("planner@example.com");
        saved.setRoles(List.of(role));

        when(userRepository.existsByUsername("planner")).thenReturn(false);
        when(userRepository.existsByEmail("planner@example.com")).thenReturn(false);
        when(roleRepository.findAllById(List.of(roleId))).thenReturn(List.of(role));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(saved));
        when(roleRepository.findAllWithPermissionsByIdIn(List.of(roleId))).thenReturn(List.of(role));

        User result = userService.createUser("planner", "secret123", " Planner@Example.com ", List.of(roleId));

        assertThat(result.getEmail()).isEqualTo("planner@example.com");
        assertThat(result.getRoles()).extracting(Role::getName).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("更新用户时重复邮箱应抛出冲突异常")
    void updateUser_whenEmailDuplicated_shouldThrowConflict() {
        UUID userId = UUID.randomUUID();
        User existing = new User();
        existing.setId(userId);
        existing.setUsername("planner");
        existing.setEmail("old@example.com");
        existing.setRoles(List.of());

        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, "taken@example.com", true, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("邮箱已存在");
    }

    @Test
    @DisplayName("重置密码时应写入加密后的密码")
    void resetPassword_shouldPersistEncodedPassword() {
        UUID userId = UUID.randomUUID();
        User existing = new User();
        existing.setId(userId);
        existing.setUsername("planner");
        existing.setRoles(List.of());

        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("reset123")).thenReturn("encoded-reset123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.resetPassword(userId, "reset123");

        assertThat(existing.getPasswordHash()).isEqualTo("encoded-reset123");
        verify(userRepository).save(existing);
        verify(authSessionService).revokeAllUserSessions(userId);
    }

    @Test
    @DisplayName("禁用用户时应撤销全部会话")
    void updateUser_whenDisabled_shouldRevokeAllSessions() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Role role = buildRole(roleId, "SUPERVISOR");
        User existing = new User();
        existing.setId(userId);
        existing.setUsername("supervisor");
        existing.setEmail("sup@example.com");
        existing.setEnabled(true);
        existing.setRoles(List.of(role));

        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(existing), Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("sup@example.com", userId)).thenReturn(false);
        when(roleRepository.findAllById(List.of(roleId))).thenReturn(List.of(role));
        when(roleRepository.findAllWithPermissionsByIdIn(List.of(roleId))).thenReturn(List.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(userId, "sup@example.com", false, List.of(roleId));

        assertThat(existing.getEnabled()).isFalse();
        verify(authSessionService).revokeAllUserSessions(userId);
    }

    @Test
    @DisplayName("分页查询未指定排序时应使用稳定默认排序")
    void getUsers_whenPageableUnsorted_shouldApplyStableSort() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.search(any(), any(), any())).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        userService.getUsers(null, null, pageable);

        verify(userRepository).search(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.argThat(argument ->
                        argument.getSort().getOrderFor("createTime") != null
                                && argument.getSort().getOrderFor("createTime").isDescending()
                                && argument.getSort().getOrderFor("id") != null
                                && argument.getSort().getOrderFor("id").isDescending()));
    }

    private Role buildRole(UUID id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setPermissions(List.of());
        return role;
    }
}
