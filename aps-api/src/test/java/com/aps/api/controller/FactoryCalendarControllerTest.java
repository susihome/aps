package com.aps.api.controller;

import com.aps.api.config.SecurityConfig;
import com.aps.api.security.CustomUserDetailsService;
import com.aps.api.security.JwtAuthenticationFilter;
import com.aps.service.FactoryCalendarService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("工厂日历控制器测试")
class FactoryCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FactoryCalendarService factoryCalendarService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(authorities = "basedata:factory-calendar:query")
    @DisplayName("具备查询权限时查询日期应返回200")
    void getDatesByMonth_whenAuthorized_shouldReturnOk() throws Exception {
        when(factoryCalendarService.getDatesByMonth(any(UUID.class), eq(2026), eq(4))).thenReturn(List.of());

        mockMvc.perform(get("/api/factory-calendars/{id}/dates", UUID.randomUUID())
                        .param("year", "2026")
                        .param("month", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "basedata:factory-calendar:edit")
    @DisplayName("空日期列表请求可到达控制器")
    void batchSetHolidays_whenPayloadSubmitted_shouldReachController() throws Exception {
        mockMvc.perform(post("/api/factory-calendars/{id}/dates/holidays", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"dates\": []
                                }
                                """))
                .andExpect(status().isOk());
    }
}
