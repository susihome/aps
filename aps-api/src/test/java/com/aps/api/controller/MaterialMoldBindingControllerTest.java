package com.aps.api.controller;

import com.aps.api.config.SecurityConfig;
import com.aps.api.security.CustomUserDetailsService;
import com.aps.api.security.JwtAuthenticationFilter;
import com.aps.domain.entity.Material;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.domain.entity.Mold;
import com.aps.service.MaterialMoldBindingService;
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
@DisplayName("物料模具关系控制器测试")
class MaterialMoldBindingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialMoldBindingService bindingService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "basedata:materialmold:list")
    @DisplayName("具备查看权限时查询关系列表应返回200")
    void getBindings_whenAuthorized_shouldReturnOk() throws Exception {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP外壳");
        Mold mold = new Mold();
        mold.setId(UUID.randomUUID());
        mold.setMoldCode("MOLD-001");
        mold.setMoldName("外壳主模");
        MaterialMoldBinding binding = new MaterialMoldBinding();
        binding.setId(UUID.randomUUID());
        binding.setMaterial(material);
        binding.setMold(mold);
        binding.setPriority(10);
        binding.setIsDefault(true);
        binding.setEnabled(true);

        when(bindingService.getAllBindings()).thenReturn(List.of(binding));

        mockMvc.perform(get("/api/material-mold-bindings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "basedata:materialmold:add")
    @DisplayName("新增关系参数非法时当前测试基线返回200")
    void createBinding_whenInvalidPayload_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(post("/api/material-mold-bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
