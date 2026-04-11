package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Mold;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MoldRepository;
import com.aps.service.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoldService {

    private final MoldRepository moldRepository;
    private final OperationRepository operationRepository;

    @Transactional(readOnly = true)
    public List<Mold> getAllMolds() {
        return moldRepository.findAllByOrderByMoldCodeAsc();
    }

    @Transactional(readOnly = true)
    public Mold getMoldById(UUID id) {
        return moldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模具不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.MOLD_CREATE, resource = "Mold")
    public Mold createMold(String moldCode, String moldName, Integer cavityCount,
                           String status, Boolean enabled, String remark) {
        String normalizedCode = normalizeCode(moldCode, "模具编码");
        if (moldRepository.existsByMoldCode(normalizedCode)) {
            throw new ResourceConflictException("模具编码已存在: " + normalizedCode);
        }
        validateCavityCount(cavityCount);

        Mold mold = new Mold();
        mold.setMoldCode(normalizedCode);
        mold.setMoldName(normalizeRequiredText(moldName, "模具名称"));
        mold.setCavityCount(cavityCount);
        mold.setStatus(normalizeNullableText(status));
        mold.setEnabled(enabled == null || enabled);
        mold.setRemark(normalizeNullableText(remark));
        return moldRepository.save(mold);
    }

    @Transactional
    @Audited(action = AuditAction.MOLD_UPDATE, resource = "Mold")
    public Mold updateMold(UUID id, String moldName, Integer cavityCount,
                           String status, Boolean enabled, String remark) {
        Mold mold = getMoldById(id);
        if (moldName != null) {
            mold.setMoldName(normalizeRequiredText(moldName, "模具名称"));
        }
        if (cavityCount != null) {
            validateCavityCount(cavityCount);
            mold.setCavityCount(cavityCount);
        }
        if (status != null) {
            mold.setStatus(normalizeNullableText(status));
        }
        if (enabled != null) {
            mold.setEnabled(enabled);
        }
        if (remark != null) {
            mold.setRemark(normalizeNullableText(remark));
        }
        return moldRepository.save(mold);
    }

    @Transactional
    @Audited(action = AuditAction.MOLD_DELETE, resource = "Mold")
    public void deleteMold(UUID id) {
        if (!moldRepository.existsById(id)) {
            throw new ResourceNotFoundException("模具不存在: " + id);
        }
        if (operationRepository.existsByRequiredMold_Id(id)) {
            throw new ResourceConflictException("该模具已被工序引用，无法删除");
        }
        moldRepository.deleteById(id);
    }

    private String normalizeCode(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeNullableText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateCavityCount(Integer cavityCount) {
        if (cavityCount != null && cavityCount <= 0) {
            throw new ResourceConflictException("模穴数必须大于0");
        }
    }
}
