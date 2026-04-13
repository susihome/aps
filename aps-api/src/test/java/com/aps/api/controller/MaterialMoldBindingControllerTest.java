package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.Material;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.domain.entity.Mold;
import com.aps.service.MaterialMoldBindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("物料模具关系控制器测试")
class MaterialMoldBindingControllerTest {

    @Mock
    private MaterialMoldBindingService bindingService;

    @InjectMocks
    private MaterialMoldBindingController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询关系列表应返回200")
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
    @DisplayName("新增关系参数非法时应返回400")
    void createBinding_whenInvalidPayload_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/material-mold-bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
