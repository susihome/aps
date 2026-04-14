package com.aps.api.controller;

import com.aps.api.dto.ResetUserPasswordRequest;
import com.aps.domain.entity.User;
import com.aps.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserControllerSecurityTest.MethodSecurityTestConfig.class)
@DisplayName("用户控制器权限测试")
class UserControllerSecurityTest {

    @org.springframework.beans.factory.annotation.Autowired
    private UserController userController;

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;

    @Test
    @WithMockUser(authorities = "system:user:list")
    @DisplayName("拥有用户列表权限时应允许查询")
    void getUsers_whenHasListAuthority_shouldAllow() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("planner");
        user.setRoles(List.of());
        when(userService.getUsers(any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(user)));

        assertThatCode(() -> userController.getAllUsers("plan", true, org.springframework.data.domain.PageRequest.of(0, 10)))
                .doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(authorities = "system:permission:list")
    @DisplayName("缺少用户列表权限时应拒绝访问")
    void getUsers_whenMissingListAuthority_shouldDeny() {
        assertThatThrownBy(() -> userController.getAllUsers(null, null, org.springframework.data.domain.PageRequest.of(0, 10)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(authorities = "system:user:edit")
    @DisplayName("缺少重置密码权限时应拒绝访问")
    void resetPassword_whenMissingAuthority_shouldDeny() {
        assertThatThrownBy(() -> userController.resetPassword(UUID.randomUUID(), new ResetUserPasswordRequest("reset123")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(authorities = "system:user:remove")
    @DisplayName("兼容旧删除权限码时应允许删除")
    void deleteUser_whenHasLegacyRemoveAuthority_shouldAllow() {
        assertThatCode(() -> userController.deleteUser(UUID.randomUUID()))
                .doesNotThrowAnyException();
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService);
        }
    }
}
