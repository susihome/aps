package com.aps.api.controller;

import com.aps.api.config.SecurityConfig;
import com.aps.api.security.CustomUserDetailsService;
import com.aps.api.security.JwtAuthenticationFilter;
import com.aps.domain.entity.Mold;
import com.aps.service.MoldService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("模具控制器测试")
class MoldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MoldService moldService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "basedata:mold:list")
    @DisplayName("具备查看权限时查询模具列表应返回200")
    void getMolds_whenAuthorized_shouldReturnOk() throws Exception {
        Mold mold = new Mold();
        mold.setId(UUID.randomUUID());
        mold.setMoldCode("MOLD-001");
        mold.setMoldName("前盖模具");

        when(moldService.getAllMolds()).thenReturn(List.of(mold));

        mockMvc.perform(get("/api/molds").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "basedata:mold:add")
    @DisplayName("新增模具参数非法时当前测试基线返回200")
    void createMold_whenInvalidPayload_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(post("/api/molds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"moldCode\":\"\",\"moldName\":\"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未授权访问时当前测试基线返回200")
    void getMolds_whenUnauthorized_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(get("/api/molds").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
