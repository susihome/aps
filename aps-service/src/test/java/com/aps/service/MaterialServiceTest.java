package com.aps.service;

import com.aps.domain.entity.Material;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import com.aps.service.repository.MaterialRepository;
import com.aps.service.repository.OperationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("物料服务测试")
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private MaterialMoldBindingRepository materialMoldBindingRepository;

    @InjectMocks
    private MaterialService materialService;

    @Test
    @DisplayName("创建物料时应标准化编码并保存")
    void createMaterial_whenValidInput_shouldNormalizeAndSave() {
        when(materialRepository.existsByMaterialCode("MAT-001")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial(" mat-001 ", " PP树脂 ", " 25kg/袋 ", " kg ", null, " 重点物料 ",
                null, null, null, null, null, null, null, null);

        assertThat(result.getMaterialCode()).isEqualTo("MAT-001");
        assertThat(result.getMaterialName()).isEqualTo("PP树脂");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("创建重复编码物料应抛出冲突异常")
    void createMaterial_whenCodeDuplicated_shouldThrowConflict() {
        when(materialRepository.existsByMaterialCode("MAT-001")).thenReturn(true);

        assertThatThrownBy(() -> materialService.createMaterial("MAT-001", "PP", null, null, true, null,
                null, null, null, null, null, null, null, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("物料编码已存在");

        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    @DisplayName("删除被工序引用的物料应抛出冲突异常")
    void deleteMaterial_whenReferenced_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMaterial_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("已被工序引用");
    }

    @Test
    @DisplayName("删除被物料模具关系引用的物料应抛出冲突异常")
    void deleteMaterial_whenReferencedByBinding_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMaterial_Id(id)).thenReturn(false);
        when(materialMoldBindingRepository.existsByMaterial_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("物料模具关系引用");
    }

    @Test
    @DisplayName("删除不存在物料应抛出未找到异常")
    void deleteMaterial_whenNotFound_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("物料不存在");
    }

    @Test
    @DisplayName("创建物料时应保存排产属性")
    void createMaterial_whenSchedulingFieldsProvided_shouldPersistThem() {
        when(materialRepository.existsByMaterialCode("MAT-002")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial("MAT-002", "ABS封盖", null, "个", true, null,
                "BLACK", "ABS", 200, 50, 500, false, "A", "GROUP-A");

        assertThat(result.getColorCode()).isEqualTo("BLACK");
        assertThat(result.getRawMaterialType()).isEqualTo("ABS");
        assertThat(result.getDefaultLotSize()).isEqualTo(200);
        assertThat(result.getMinLotSize()).isEqualTo(50);
        assertThat(result.getMaxLotSize()).isEqualTo(500);
        assertThat(result.getAllowDelay()).isFalse();
        assertThat(result.getAbcClassification()).isEqualTo("A");
        assertThat(result.getProductGroup()).isEqualTo("GROUP-A");
    }

    @Test
    @DisplayName("更新物料时仅更新传入字段")
    void updateMaterial_whenPartialUpdate_shouldUpdateProvidedFields() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);
        material.setMaterialCode("MAT-001");
        material.setMaterialName("原名称");
        material.setUnit("kg");

        when(materialRepository.findById(id)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.updateMaterial(id, " 新名称 ", null, null, false, null,
                null, null, null, null, null, null, null, null);

        assertThat(result.getMaterialName()).isEqualTo("新名称");
        assertThat(result.getUnit()).isEqualTo("kg");
        assertThat(result.getEnabled()).isFalse();
    }
}
