package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.service.FactoryCalendarService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("工厂日历控制器测试")
class FactoryCalendarControllerTest {

    @Mock
    private FactoryCalendarService factoryCalendarService;

    @InjectMocks
    private FactoryCalendarController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询日期应返回200")
    void getDatesByMonth_whenAuthorized_shouldReturnOk() throws Exception {
        when(factoryCalendarService.getDatesByMonth(any(UUID.class), eq(2026), eq(4))).thenReturn(List.of());

        mockMvc.perform(get("/api/factory-calendars/{id}/dates", UUID.randomUUID())
                        .param("year", "2026")
                        .param("month", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("空日期列表请求应返回400")
    void batchSetHolidays_whenPayloadSubmitted_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/factory-calendars/{id}/dates/holidays", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dates": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
