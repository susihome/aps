package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Material;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.domain.entity.Mold;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialMoldBindingService {

    private final MaterialMoldBindingRepository bindingRepository;
    private final MaterialService materialService;
    private final MoldService moldService;

    @Transactional(readOnly = true)
    public List<MaterialMoldBinding> getAllBindings() {
        return bindingRepository.findAllByOrderByPriorityDescCreateTimeAsc();
    }

    @Transactional(readOnly = true)
    public MaterialMoldBinding getBindingById(UUID id) {
        return bindingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("物料模具关系不存在: " + id));
    }

    @Transactional(readOnly = true)
    public List<MaterialMoldBinding> getBindingsByMaterial(UUID materialId) {
        materialService.getMaterialById(materialId);
        return bindingRepository.findAllByMaterial_IdAndEnabledTrueOrderByIsDefaultDescIsPreferredDescPriorityDescCreateTimeAsc(materialId);
    }

    @Transactional(readOnly = true)
    public List<MaterialMoldBinding> getBindingsByMold(UUID moldId) {
        moldService.getMoldById(moldId);
        return bindingRepository.findAllByMold_IdAndEnabledTrueOrderByIsDefaultDescIsPreferredDescPriorityDescCreateTimeAsc(moldId);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_MOLD_BINDING_CREATE, resource = "MaterialMoldBinding")
    public MaterialMoldBinding createBinding(UUID materialId, UUID moldId, Integer priority,
                                             Boolean isDefault, Boolean isPreferred,
                                             Integer cycleTimeMinutes, Integer setupTimeMinutes,
                                             Integer changeoverTimeMinutes, Boolean enabled,
                                             LocalDateTime validFrom, LocalDateTime validTo,
                                             String remark) {
        if (bindingRepository.existsByMaterial_IdAndMold_Id(materialId, moldId)) {
            throw new ResourceConflictException("该物料与模具关系已存在");
        }

        Material material = materialService.getMaterialById(materialId);
        Mold mold = moldService.getMoldById(moldId);
        validateTimes(priority, cycleTimeMinutes, setupTimeMinutes, changeoverTimeMinutes, validFrom, validTo);

        MaterialMoldBinding binding = new MaterialMoldBinding();
        binding.setMaterial(material);
        binding.setMold(mold);
        applyFields(binding, priority, isDefault, isPreferred, cycleTimeMinutes, setupTimeMinutes,
                changeoverTimeMinutes, enabled, validFrom, validTo, remark);
        return bindingRepository.save(binding);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_MOLD_BINDING_UPDATE, resource = "MaterialMoldBinding")
    public MaterialMoldBinding updateBinding(UUID id, Integer priority,
                                             Boolean isDefault, Boolean isPreferred,
                                             Integer cycleTimeMinutes, Integer setupTimeMinutes,
                                             Integer changeoverTimeMinutes, Boolean enabled,
                                             LocalDateTime validFrom, LocalDateTime validTo,
                                             String remark) {
        MaterialMoldBinding binding = getBindingById(id);
        validateTimes(priority != null ? priority : binding.getPriority(),
                cycleTimeMinutes != null ? cycleTimeMinutes : binding.getCycleTimeMinutes(),
                setupTimeMinutes != null ? setupTimeMinutes : binding.getSetupTimeMinutes(),
                changeoverTimeMinutes != null ? changeoverTimeMinutes : binding.getChangeoverTimeMinutes(),
                validFrom != null ? validFrom : binding.getValidFrom(),
                validTo != null ? validTo : binding.getValidTo());

        applyUpdates(binding, priority, isDefault, isPreferred, cycleTimeMinutes, setupTimeMinutes,
                changeoverTimeMinutes, enabled, validFrom, validTo, remark);
        return bindingRepository.save(binding);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_MOLD_BINDING_DELETE, resource = "MaterialMoldBinding")
    public void deleteBinding(UUID id) {
        if (!bindingRepository.existsById(id)) {
            throw new ResourceNotFoundException("物料模具关系不存在: " + id);
        }
        bindingRepository.deleteById(id);
    }

    private void applyFields(MaterialMoldBinding binding, Integer priority,
                             Boolean isDefault, Boolean isPreferred,
                             Integer cycleTimeMinutes, Integer setupTimeMinutes,
                             Integer changeoverTimeMinutes, Boolean enabled,
                             LocalDateTime validFrom, LocalDateTime validTo,
                             String remark) {
        binding.setPriority(priority != null ? priority : Integer.valueOf(0));
        binding.setIsDefault(Boolean.TRUE.equals(isDefault));
        binding.setIsPreferred(Boolean.TRUE.equals(isPreferred));
        binding.setCycleTimeMinutes(cycleTimeMinutes);
        binding.setSetupTimeMinutes(setupTimeMinutes);
        binding.setChangeoverTimeMinutes(changeoverTimeMinutes);
        binding.setEnabled(enabled == null || enabled);
        binding.setValidFrom(validFrom);
        binding.setValidTo(validTo);
        binding.setRemark(normalizeNullableText(remark));
    }

    private void applyUpdates(MaterialMoldBinding binding, Integer priority,
                              Boolean isDefault, Boolean isPreferred,
                              Integer cycleTimeMinutes, Integer setupTimeMinutes,
                              Integer changeoverTimeMinutes, Boolean enabled,
                              LocalDateTime validFrom, LocalDateTime validTo,
                              String remark) {
        if (priority != null) {
            binding.setPriority(priority);
        }
        if (isDefault != null) {
            binding.setIsDefault(isDefault);
        }
        if (isPreferred != null) {
            binding.setIsPreferred(isPreferred);
        }
        if (cycleTimeMinutes != null) {
            binding.setCycleTimeMinutes(cycleTimeMinutes);
        }
        if (setupTimeMinutes != null) {
            binding.setSetupTimeMinutes(setupTimeMinutes);
        }
        if (changeoverTimeMinutes != null) {
            binding.setChangeoverTimeMinutes(changeoverTimeMinutes);
        }
        if (enabled != null) {
            binding.setEnabled(enabled);
        }
        if (validFrom != null) {
            binding.setValidFrom(validFrom);
        }
        if (validTo != null) {
            binding.setValidTo(validTo);
        }
        if (remark != null) {
            binding.setRemark(normalizeNullableText(remark));
        }
    }

    private void validateTimes(Integer priority, Integer cycleTimeMinutes, Integer setupTimeMinutes,
                               Integer changeoverTimeMinutes, LocalDateTime validFrom, LocalDateTime validTo) {
        if (priority != null && priority < 0) {
            throw new ResourceConflictException("优先级不能小于0");
        }
        if (cycleTimeMinutes != null && cycleTimeMinutes <= 0) {
            throw new ResourceConflictException("节拍时间必须大于0");
        }
        if (setupTimeMinutes != null && setupTimeMinutes < 0) {
            throw new ResourceConflictException("上模时间不能小于0");
        }
        if (changeoverTimeMinutes != null && changeoverTimeMinutes < 0) {
            throw new ResourceConflictException("换模时间不能小于0");
        }
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new ResourceConflictException("失效时间不能早于生效时间");
        }
    }

    private String normalizeNullableText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
