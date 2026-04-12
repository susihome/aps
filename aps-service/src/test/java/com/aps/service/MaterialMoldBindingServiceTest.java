package com.aps.service;

import com.aps.domain.entity.Material;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.domain.entity.Mold;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("物料模具关系服务测试")
class MaterialMoldBindingServiceTest {

    @Mock
    private MaterialMoldBindingRepository bindingRepository;

    @Mock
    private MaterialService materialService;

    @Mock
    private MoldService moldService;

    @InjectMocks
    private MaterialMoldBindingService bindingService;

    @Test
    @DisplayName("创建关系时应保存优先属性")
    void createBinding_whenValid_shouldPersistSchedulingFields() {
        UUID materialId = UUID.randomUUID();
        UUID moldId = UUID.randomUUID();
        Material material = new Material();
        material.setId(materialId);
        material.setMaterialCode("MAT-001");
        Mold mold = new Mold();
        mold.setId(moldId);
        mold.setMoldCode("MOLD-001");

        when(bindingRepository.existsByMaterial_IdAndMold_Id(materialId, moldId)).thenReturn(false);
        when(materialService.getMaterialById(materialId)).thenReturn(material);
        when(moldService.getMoldById(moldId)).thenReturn(mold);
        when(bindingRepository.save(any(MaterialMoldBinding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaterialMoldBinding result = bindingService.createBinding(materialId, moldId, 10,
                true, true, 15, 20, 30, true,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 0, 0),
                "量产主模");

        assertThat(result.getPriority()).isEqualTo(10);
        assertThat(result.getIsDefault()).isTrue();
        assertThat(result.getIsPreferred()).isTrue();
        assertThat(result.getCycleTimeMinutes()).isEqualTo(15);
        assertThat(result.getSetupTimeMinutes()).isEqualTo(20);
        assertThat(result.getChangeoverTimeMinutes()).isEqualTo(30);
        assertThat(result.getRemark()).isEqualTo("量产主模");
    }

    @Test
    @DisplayName("创建重复关系时应抛出冲突异常")
    void createBinding_whenDuplicated_shouldThrowConflict() {
        UUID materialId = UUID.randomUUID();
        UUID moldId = UUID.randomUUID();
        when(bindingRepository.existsByMaterial_IdAndMold_Id(materialId, moldId)).thenReturn(true);

        assertThatThrownBy(() -> bindingService.createBinding(materialId, moldId, 1,
                false, false, null, null, null, true, null, null, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("关系已存在");

        verify(bindingRepository, never()).save(any(MaterialMoldBinding.class));
    }

    @Test
    @DisplayName("更新时间范围非法时应抛出冲突异常")
    void updateBinding_whenInvalidDateRange_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        MaterialMoldBinding binding = new MaterialMoldBinding();
        binding.setId(id);

        when(bindingRepository.findById(id)).thenReturn(Optional.of(binding));

        assertThatThrownBy(() -> bindingService.updateBinding(id, 1, null, null,
                null, null, null, null,
                LocalDateTime.of(2026, 4, 2, 0, 0),
                LocalDateTime.of(2026, 4, 1, 0, 0), null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("失效时间不能早于生效时间");
    }

    @Test
    @DisplayName("删除不存在关系时应抛出未找到异常")
    void deleteBinding_whenNotFound_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(bindingRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> bindingService.deleteBinding(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("物料模具关系不存在");
    }
}
