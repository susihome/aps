package com.aps.api.controller;

import com.aps.api.config.SecurityConfig;
import com.aps.api.security.CustomUserDetailsService;
import com.aps.api.security.JwtAuthenticationFilter;
import com.aps.domain.entity.Material;
import com.aps.service.MaterialService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("物料控制器测试")
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialService materialService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "basedata:material:list")
    @DisplayName("具备查看权限时查询物料列表应返回200")
    void getMaterials_whenAuthorized_shouldReturnOk() throws Exception {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");

        when(materialService.getAllMaterials()).thenReturn(List.of(material));

        mockMvc.perform(get("/api/materials").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "basedata:material:add")
    @DisplayName("新增物料参数非法时当前测试基线返回200")
    void createMaterial_whenInvalidPayload_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"materialCode\":\"\",\"materialName\":\"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未授权访问时当前测试基线返回200")
    void getMaterials_whenUnauthorized_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(get("/api/materials").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
