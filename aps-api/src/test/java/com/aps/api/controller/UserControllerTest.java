package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("分页查询用户应返回统一分页结构")
    void getAllUsers_shouldReturnPagedResult() throws Exception {
        User user = buildUser("planner", "planner@example.com", "PLANNER");
        when(userService.getUsers(eq("plan"), eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users")
                        .param("keyword", "plan")
                        .param("enabled", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("planner"))
                .andExpect(jsonPath("$.data.content[0].roles[0]").value("PLANNER"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(userService).getUsers(eq("plan"), eq(true), org.mockito.ArgumentMatchers.argThat(pageable ->
                pageable.getSort().getOrderFor("createTime") != null
                        && pageable.getSort().getOrderFor("id") != null));
    }

    @Test
    @DisplayName("创建用户时应透传角色列表")
    void createUser_shouldPassRoleIds() throws Exception {
        UUID roleId = UUID.randomUUID();
        User user = buildUser("admin2", "admin2@example.com", "ADMIN");
        when(userService.createUser(eq("admin2"), eq("secret123"), eq("admin2@example.com"), eq(List.of(roleId))))
                .thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "admin2",
                                  "password": "secret123",
                                  "email": "admin2@example.com",
                                  "roleIds": ["%s"]
                                }
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin2"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("更新用户时应支持角色和状态变更")
    void updateUser_shouldPassRoleIdsAndEnabled() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        User user = buildUser("supervisor", "sup@example.com", "SUPERVISOR");
        user.setEnabled(false);

        when(userService.updateUser(userId, "sup@example.com", false, List.of(roleId))).thenReturn(user);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "sup@example.com",
                                  "enabled": false,
                                  "roleIds": ["%s"]
                                }
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    @DisplayName("重置密码时应调用服务")
    void resetPassword_shouldInvokeService() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/users/{id}/reset-password", userId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "newPassword": "reset123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).resetPassword(userId, "reset123");
    }

    private User buildUser(String username, String email, String roleName) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(roleName);
        role.setPermissions(List.of());
        user.setRoles(List.of(role));
        return user;
    }
}
