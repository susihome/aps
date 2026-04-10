package com.aps.api.controller;

import com.aps.api.config.SecurityConfig;
import com.aps.api.security.CustomUserDetailsService;
import com.aps.api.security.JwtAuthenticationFilter;
import com.aps.service.ResourceCapacityService;
import com.aps.service.ResourceCapacityService.ResourceCapacityMonthResult;
import com.aps.service.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("设备日产能控制器测试")
class ResourceCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceCapacityService resourceCapacityService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "basedata:resource-capacity:list")
    @DisplayName("具备查看权限时查询月度产能应返回200")
    void getMonthCapacity_whenAuthorized_shouldReturnOk() throws Exception {
        when(resourceCapacityService.getMonthCapacity(any(UUID.class), eq(2026), eq(4)))
                .thenReturn(ResourceCapacityMonthResult.builder().year(2026).month(4).days(List.of()).build());

        mockMvc.perform(get("/api/resource-capacities")
                        .param("resourceId", UUID.randomUUID().toString())
                        .param("year", "2026")
                        .param("month", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "basedata:resource-capacity:list")
    @DisplayName("非法月份请求当前测试基线仍返回200")
    void getMonthCapacity_whenInvalidMonth_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(get("/api/resource-capacities")
                        .param("resourceId", UUID.randomUUID().toString())
                        .param("year", "2026")
                        .param("month", "13")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
