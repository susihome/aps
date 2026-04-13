package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.service.ResourceCapacityService;
import com.aps.service.ResourceCapacityService.ResourceCapacityMonthResult;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("设备日产能控制器测试")
class ResourceCapacityControllerTest {

    @Mock
    private ResourceCapacityService resourceCapacityService;

    @InjectMocks
    private ResourceCapacityController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询月度产能应返回200")
    void getMonthCapacity_whenAuthorized_shouldReturnOk() throws Exception {
        UUID resourceId = UUID.randomUUID();
        when(resourceCapacityService.getMonthCapacity(eq(resourceId), eq(2026), eq(4)))
                .thenReturn(ResourceCapacityMonthResult.builder().year(2026).month(4).days(List.of()).build());

        mockMvc.perform(get("/api/resource-capacities")
                        .param("resourceId", resourceId.toString())
                        .param("year", "2026")
                        .param("month", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("非法月份请求当前基线返回200")
    void getMonthCapacity_whenInvalidMonth_shouldReturnCurrentBaselineStatus() throws Exception {
        mockMvc.perform(get("/api/resource-capacities")
                        .param("resourceId", UUID.randomUUID().toString())
                        .param("year", "2026")
                        .param("month", "13")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
