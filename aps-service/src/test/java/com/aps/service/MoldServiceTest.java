package com.aps.service;

import com.aps.domain.entity.Mold;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import com.aps.service.repository.MoldRepository;
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
@DisplayName("模具服务测试")
class MoldServiceTest {

    @Mock
    private MoldRepository moldRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private MaterialMoldBindingRepository materialMoldBindingRepository;

    @InjectMocks
    private MoldService moldService;

    @Test
    @DisplayName("创建模具时应标准化编码并保存")
    void createMold_whenValidInput_shouldNormalizeAndSave() {
        when(moldRepository.existsByMoldCode("MOLD-001")).thenReturn(false);
        when(moldRepository.save(any(Mold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mold result = moldService.createMold(" mold-001 ", " 前盖模具 ", 8, "ACTIVE", null, " 核心模具 ",
                350, null, "READY");

        assertThat(result.getMoldCode()).isEqualTo("MOLD-001");
        assertThat(result.getMoldName()).isEqualTo("前盖模具");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getRequiredTonnage()).isEqualTo(350);
        assertThat(result.getMaintenanceState()).isEqualTo("READY");
    }

    @Test
    @DisplayName("创建重复编码模具应抛出冲突异常")
    void createMold_whenCodeDuplicated_shouldThrowConflict() {
        when(moldRepository.existsByMoldCode("MOLD-001")).thenReturn(true);

        assertThatThrownBy(() -> moldService.createMold("MOLD-001", "模具A", 4, "ACTIVE", true, null,
                null, null, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("模具编码已存在");

        verify(moldRepository, never()).save(any(Mold.class));
    }

    @Test
    @DisplayName("创建模具时模穴数不合法应抛出冲突异常")
    void createMold_whenInvalidCavityCount_shouldThrowConflict() {
        when(moldRepository.existsByMoldCode("MOLD-001")).thenReturn(false);

        assertThatThrownBy(() -> moldService.createMold("MOLD-001", "模具A", 0, "ACTIVE", true, null,
                null, null, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("模穴数必须大于0");
    }

    @Test
    @DisplayName("删除被工序引用的模具应抛出冲突异常")
    void deleteMold_whenReferenced_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(moldRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMold_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> moldService.deleteMold(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("已被工序引用");
    }

    @Test
    @DisplayName("删除被物料模具关系引用的模具应抛出冲突异常")
    void deleteMold_whenReferencedByBinding_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(moldRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMold_Id(id)).thenReturn(false);
        when(materialMoldBindingRepository.existsByMold_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> moldService.deleteMold(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("物料模具关系引用");
    }

    @Test
    @DisplayName("删除不存在模具应抛出未找到异常")
    void deleteMold_whenNotFound_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(moldRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> moldService.deleteMold(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("模具不存在");
    }

    @Test
    @DisplayName("更新模具时仅更新传入字段")
    void updateMold_whenPartialUpdate_shouldUpdateProvidedFields() {
        UUID id = UUID.randomUUID();
        Mold mold = new Mold();
        mold.setId(id);
        mold.setMoldCode("MOLD-001");
        mold.setMoldName("原模具");
        mold.setStatus("ACTIVE");

        when(moldRepository.findById(id)).thenReturn(Optional.of(mold));
        when(moldRepository.save(any(Mold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mold result = moldService.updateMold(id, " 新模具 ", null, null, false, null,
                400, null, "MAINTAINING");

        assertThat(result.getMoldName()).isEqualTo("新模具");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getRequiredTonnage()).isEqualTo(400);
        assertThat(result.getMaintenanceState()).isEqualTo("MAINTAINING");
    }
}
