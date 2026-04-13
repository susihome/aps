package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.DictItem;
import com.aps.domain.entity.DictType;
import com.aps.service.DictItemService;
import com.aps.service.DictTypeService;
import com.aps.service.exception.ResourceConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("编码管理控制器测试")
class DictionaryControllerTest {

    @Mock
    private DictTypeService dictTypeService;

    @Mock
    private DictItemService dictItemService;

    @InjectMocks
    private DictionaryController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询字典类型列表应返回统一分页结构")
    void getTypes_shouldReturnPageResult() throws Exception {
        DictType type = new DictType();
        UUID id = UUID.randomUUID();
        type.setId(id);
        type.setCode("ORDER_STATUS");
        type.setName("订单状态");
        type.setEnabled(true);
        type.setSortOrder(1);

        when(dictTypeService.getTypes(eq("order"), eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(type), PageRequest.of(0, 10, Sort.by("sortOrder").ascending().and(Sort.by("id").ascending())), 1));

        mockMvc.perform(get("/api/dictionaries/types")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("keyword", "order")
                        .param("enabled", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增字典类型时参数非法应返回400")
    void createType_whenInvalidPayload_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/dictionaries/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("新增重复字典类型应返回409")
    void createType_whenDuplicated_shouldReturn409() throws Exception {
        when(dictTypeService.createType(eq("ORDER_STATUS"), eq("订单状态"), eq(null), eq(true), eq(1)))
                .thenThrow(new ResourceConflictException("DICT_TYPE_CODE_DUPLICATE: 字典类型编码已存在"));

        mockMvc.perform(post("/api/dictionaries/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "ORDER_STATUS",
                                  "name": "订单状态",
                                  "enabled": true,
                                  "sortOrder": 1
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("类型启停缺少enabled参数应返回200错误包")
    void toggleTypeEnabled_whenMissingEnabled_shouldReturnErrorEnvelope() throws Exception {
        mockMvc.perform(patch("/api/dictionaries/types/{id}/enabled", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("删除存在字典项的类型应返回409")
    void deleteType_whenHasItems_shouldReturn409() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceConflictException("DICT_TYPE_HAS_ITEMS: 字典类型下存在字典项，无法删除"))
                .when(dictTypeService).deleteType(id);

        mockMvc.perform(delete("/api/dictionaries/types/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("查询字典项列表应返回统一分页结构")
    void getItemsByType_shouldReturnPageResult() throws Exception {
        UUID typeId = UUID.randomUUID();
        DictType type = new DictType();
        type.setId(typeId);
        type.setCode("ORDER_STATUS");

        DictItem item = new DictItem();
        item.setId(UUID.randomUUID());
        item.setDictType(type);
        item.setItemCode("PENDING");
        item.setItemName("待排产");
        item.setItemValue("PENDING");
        item.setEnabled(true);
        item.setSortOrder(1);
        item.setIsSystem(true);

        when(dictItemService.getItemsByType(eq(typeId), eq("pending"), eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10, Sort.by("sortOrder").ascending().and(Sort.by("id").ascending())), 1));

        mockMvc.perform(get("/api/dictionaries/types/{typeId}/items", typeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("keyword", "pending")
                        .param("enabled", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增字典项时参数非法应返回400")
    void createItem_whenInvalidPayload_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/dictionaries/types/{typeId}/items", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"\",\"itemName\":\"\",\"itemValue\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("字典项启停缺少enabled参数应返回200错误包")
    void toggleItemEnabled_whenMissingEnabled_shouldReturnErrorEnvelope() throws Exception {
        mockMvc.perform(patch("/api/dictionaries/items/{id}/enabled", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("按类型编码查询启用项应返回列表")
    void getEnabledItemsByTypeCode_shouldReturnList() throws Exception {
        DictType type = new DictType();
        type.setId(UUID.randomUUID());
        type.setCode("ORDER_STATUS");

        DictItem item = new DictItem();
        item.setId(UUID.randomUUID());
        item.setDictType(type);
        item.setItemCode("PENDING");
        item.setItemName("待排产");
        item.setItemValue("PENDING");
        item.setEnabled(true);
        item.setIsSystem(true);
        item.setSortOrder(1);

        when(dictItemService.getEnabledItemsByTypeCode("order_status")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/dictionaries/{typeCode}/enabled-items", "order_status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
