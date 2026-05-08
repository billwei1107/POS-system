/**
 * @file LeaveTypeService.java
 * @description 假別類型管理服務 / Leave type management service
 * @description_en CRUD service for leave types and policies
 * @description_zh 假別類型與政策的 CRUD 服務
 */
package com.enterprise.leave.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.leave.entity.LeaveType;
import com.enterprise.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional(readOnly = true)
    public List<LeaveType> listAll() {
        return leaveTypeRepository.findAllActive();
    }

    @Transactional
    public LeaveType create(LeaveType type) {
        leaveTypeRepository.findByCodeAndDeletedAtIsNull(type.getCode()).ifPresent(existing -> {
            throw new BusinessException("LEAVE_TYPE_CODE_EXISTS: Leave type code already exists: " + type.getCode());
        });
        return leaveTypeRepository.save(type);
    }

    @Transactional
    public LeaveType update(UUID id, LeaveType patch) {
        LeaveType type = findOrThrow(id);
        type.setName(patch.getName());
        type.setPaidType(patch.getPaidType());
        type.setRequireAttachment(patch.isRequireAttachment());
        type.setMaxDaysPerYear(patch.getMaxDaysPerYear());
        return leaveTypeRepository.save(type);
    }

    @Transactional
    public void delete(UUID id) {
        LeaveType type = findOrThrow(id);
        type.setDeletedAt(LocalDateTime.now());
        leaveTypeRepository.save(type);
    }

    private LeaveType findOrThrow(UUID id) {
        return leaveTypeRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("LEAVE_TYPE_NOT_FOUND: Leave type not found: " + id));
    }
}
