package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.Mold;
import com.aps.service.MoldService;
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
@DisplayName("模具控制器测试")
class MoldControllerTest {

    @Mock
    private MoldService moldService;

    @InjectMocks
    private MoldController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询模具列表应返回200")
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
    @DisplayName("新增模具参数非法时应返回400")
    void createMold_whenInvalidPayload_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/molds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"moldCode\":\"\",\"moldName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
