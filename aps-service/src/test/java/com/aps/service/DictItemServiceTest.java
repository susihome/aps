package com.aps.service;

import com.aps.domain.entity.DictItem;
import com.aps.domain.entity.DictType;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.DictItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("字典项服务测试")
class DictItemServiceTest {

    @Mock
    private DictItemRepository dictItemRepository;

    @Mock
    private DictTypeService dictTypeService;

    @InjectMocks
    private DictItemService dictItemService;

    private UUID dictTypeId;
    private UUID dictItemId;
    private DictType dictType;
    private DictItem dictItem;

    @BeforeEach
    void setUp() {
        dictTypeId = UUID.randomUUID();
        dictItemId = UUID.randomUUID();

        dictType = new DictType();
        dictType.setId(dictTypeId);
        dictType.setCode("ORDER_STATUS");
        dictType.setName("订单状态");

        dictItem = new DictItem();
        dictItem.setId(dictItemId);
        dictItem.setDictType(dictType);
        dictItem.setItemCode("PENDING");
        dictItem.setItemName("待排产");
        dictItem.setItemValue("PENDING");
        dictItem.setEnabled(true);
        dictItem.setIsSystem(false);
        dictItem.setSortOrder(1);
    }

    @Test
    @DisplayName("按类型分页查询应返回仓库结果")
    void getItemsByType_ShouldReturnRepositoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(dictTypeService.getTypeById(dictTypeId)).thenReturn(dictType);
        when(dictItemRepository.searchByTypeId(dictTypeId, "pending", true, pageable))
                .thenReturn(new PageImpl<>(List.of(dictItem)));

        var page = dictItemService.getItemsByType(dictTypeId, "pending", true, pageable);

        assertThat(page.getContent()).containsExactly(dictItem);
    }

    @Nested
    @DisplayName("创建字典项")
    class CreateItemTests {

        @Test
        @DisplayName("同类型重复编码应抛出冲突异常")
        void createItem_whenDuplicateCodeInSameType_shouldThrowConflictException() {
            when(dictTypeService.getTypeById(dictTypeId)).thenReturn(dictType);
            when(dictItemRepository.existsByDictTypeIdAndItemCode(dictTypeId, "PENDING")).thenReturn(true);

            assertThatThrownBy(() -> dictItemService.createItem(dictTypeId, " pending ", "待排产", "PENDING", null, true, 1, false))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("DICT_ITEM_CODE_DUPLICATE");

            verify(dictItemRepository, never()).save(any(DictItem.class));
        }

        @Test
        @DisplayName("有效输入应标准化并保存")
        void createItem_whenValidInput_shouldNormalizeAndSave() {
            when(dictTypeService.getTypeById(dictTypeId)).thenReturn(dictType);
            when(dictItemRepository.existsByDictTypeIdAndItemCode(dictTypeId, "PENDING")).thenReturn(false);
            when(dictItemRepository.save(any(DictItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            DictItem result = dictItemService.createItem(dictTypeId, " pending ", " 待排产 ", " PENDING ", " 描述 ", null, null, null);

            assertThat(result.getItemCode()).isEqualTo("PENDING");
            assertThat(result.getItemName()).isEqualTo("待排产");
            assertThat(result.getItemValue()).isEqualTo("PENDING");
            assertThat(result.getDescription()).isEqualTo("描述");
            assertThat(result.getEnabled()).isTrue();
            assertThat(result.getIsSystem()).isFalse();
        }
    }

    @Nested
    @DisplayName("删除字典项")
    class DeleteItemTests {

        @Test
        @DisplayName("系统项应禁止删除")
        void deleteItem_whenSystemItem_shouldThrowConflictException() {
            dictItem.setIsSystem(true);
            when(dictItemRepository.findById(dictItemId)).thenReturn(java.util.Optional.of(dictItem));

            assertThatThrownBy(() -> dictItemService.deleteItem(dictItemId))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("不允许删除");

            verify(dictItemRepository, never()).delete(any(DictItem.class));
        }

        @Test
        @DisplayName("不存在时应抛出未找到异常")
        void deleteItem_whenNotFound_shouldThrowNotFoundException() {
            when(dictItemRepository.findById(dictItemId)).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> dictItemService.deleteItem(dictItemId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("字典项不存在");
        }
    }

    @Nested
    @DisplayName("启停字典项")
    class ToggleItemTests {

        @Test
        @DisplayName("重复启用应保持幂等")
        void toggleItemEnabled_whenAlreadyEnabled_shouldReturnSameEntity() {
            when(dictItemRepository.findById(dictItemId)).thenReturn(java.util.Optional.of(dictItem));

            DictItem result = dictItemService.toggleItemEnabled(dictItemId, true);

            assertThat(result).isSameAs(dictItem);
            verify(dictItemRepository, never()).save(any(DictItem.class));
        }

        @Test
        @DisplayName("状态变更时应保存")
        void toggleItemEnabled_whenStateChanges_shouldSaveUpdatedEntity() {
            when(dictItemRepository.findById(dictItemId)).thenReturn(java.util.Optional.of(dictItem));
            when(dictItemRepository.save(any(DictItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            DictItem result = dictItemService.toggleItemEnabled(dictItemId, false);

            assertThat(result.getEnabled()).isFalse();
            verify(dictItemRepository).save(eq(dictItem));
        }
    }

    @Test
    @DisplayName("按类型编码查询启用项应转大写后调用仓库")
    void getEnabledItemsByTypeCode_ShouldNormalizeTypeCode() {
        when(dictTypeService.getTypeByCode("ORDER_STATUS")).thenReturn(dictType);
        when(dictItemRepository.findEnabledItemsByTypeCode("ORDER_STATUS")).thenReturn(List.of(dictItem));

        List<DictItem> result = dictItemService.getEnabledItemsByTypeCode("order_status");

        assertThat(result).containsExactly(dictItem);
    }
}
