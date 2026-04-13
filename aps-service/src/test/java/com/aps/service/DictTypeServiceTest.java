package com.aps.service;

import com.aps.domain.entity.DictType;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.DictItemRepository;
import com.aps.service.repository.DictTypeRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("字典类型服务测试")
class DictTypeServiceTest {

    @Mock
    private DictTypeRepository dictTypeRepository;

    @Mock
    private DictItemRepository dictItemRepository;

    @InjectMocks
    private DictTypeService dictTypeService;

    private UUID dictTypeId;
    private DictType dictType;

    @BeforeEach
    void setUp() {
        dictTypeId = UUID.randomUUID();
        dictType = new DictType();
        dictType.setId(dictTypeId);
        dictType.setCode("ORDER_STATUS");
        dictType.setName("订单状态");
        dictType.setEnabled(true);
        dictType.setSortOrder(1);
    }

    @Test
    @DisplayName("分页查询字典类型应返回仓库结果")
    void getTypes_ShouldReturnRepositoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(dictTypeRepository.search("order", true, pageable)).thenReturn(new PageImpl<>(List.of(dictType)));

        var page = dictTypeService.getTypes("order", true, pageable);

        assertThat(page.getContent()).containsExactly(dictType);
    }

    @Nested
    @DisplayName("创建字典类型")
    class CreateTypeTests {

        @Test
        @DisplayName("重复编码应抛出冲突异常")
        void createType_whenCodeDuplicated_shouldThrowConflictException() {
            when(dictTypeRepository.existsByCode("ORDER_STATUS")).thenReturn(true);

            assertThatThrownBy(() -> dictTypeService.createType(" order_status ", "订单状态", null, true, 1))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("DICT_TYPE_CODE_DUPLICATE");

            verify(dictTypeRepository, never()).save(any(DictType.class));
        }

        @Test
        @DisplayName("空编码应抛出异常")
        void createType_whenCodeBlank_shouldThrowConflictException() {
            assertThatThrownBy(() -> dictTypeService.createType("   ", "订单状态", null, true, 1))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("字典类型编码不能为空");
        }

        @Test
        @DisplayName("有效输入应标准化并保存")
        void createType_whenValidInput_shouldNormalizeAndSave() {
            when(dictTypeRepository.existsByCode("ORDER_STATUS")).thenReturn(false);
            when(dictTypeRepository.save(any(DictType.class))).thenAnswer(invocation -> invocation.getArgument(0));

            DictType result = dictTypeService.createType(" order_status ", " 订单状态 ", " 描述 ", null, null);

            assertThat(result.getCode()).isEqualTo("ORDER_STATUS");
            assertThat(result.getName()).isEqualTo("订单状态");
            assertThat(result.getDescription()).isEqualTo("描述");
            assertThat(result.getEnabled()).isTrue();
            assertThat(result.getSortOrder()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("删除字典类型")
    class DeleteTypeTests {

        @Test
        @DisplayName("存在字典项时应禁止删除")
        void deleteType_whenHasItems_shouldThrowConflictException() {
            when(dictTypeRepository.findById(dictTypeId)).thenReturn(Optional.of(dictType));
            when(dictItemRepository.countByDictTypeId(dictTypeId)).thenReturn(2L);

            assertThatThrownBy(() -> dictTypeService.deleteType(dictTypeId))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("DICT_TYPE_HAS_ITEMS");

            verify(dictTypeRepository, never()).delete(any(DictType.class));
        }

        @Test
        @DisplayName("不存在时应抛出未找到异常")
        void deleteType_whenNotFound_shouldThrowNotFoundException() {
            when(dictTypeRepository.findById(dictTypeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dictTypeService.deleteType(dictTypeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("字典类型不存在");
        }
    }

    @Nested
    @DisplayName("启停字典类型")
    class ToggleTypeTests {

        @Test
        @DisplayName("重复启用应保持幂等")
        void toggleTypeEnabled_whenAlreadyEnabled_shouldReturnSameEntity() {
            when(dictTypeRepository.findById(dictTypeId)).thenReturn(Optional.of(dictType));

            DictType result = dictTypeService.toggleTypeEnabled(dictTypeId, true);

            assertThat(result).isSameAs(dictType);
            verify(dictTypeRepository, never()).save(any(DictType.class));
        }

        @Test
        @DisplayName("状态变更时应保存")
        void toggleTypeEnabled_whenStateChanges_shouldSaveUpdatedEntity() {
            when(dictTypeRepository.findById(dictTypeId)).thenReturn(Optional.of(dictType));
            when(dictTypeRepository.save(any(DictType.class))).thenAnswer(invocation -> invocation.getArgument(0));

            DictType result = dictTypeService.toggleTypeEnabled(dictTypeId, false);

            assertThat(result.getEnabled()).isFalse();
            verify(dictTypeRepository).save(eq(dictType));
        }
    }
}
