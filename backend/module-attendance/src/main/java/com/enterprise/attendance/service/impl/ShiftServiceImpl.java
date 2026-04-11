package com.enterprise.attendance.service.impl;

import com.enterprise.attendance.entity.ShiftAssignment;
import com.enterprise.attendance.entity.ShiftSchedule;
import com.enterprise.attendance.repository.ShiftAssignmentRepository;
import com.enterprise.attendance.repository.ShiftScheduleRepository;
import com.enterprise.attendance.service.ShiftService;
import com.enterprise.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @file ShiftServiceImpl.java
 * @description 班表管理服務實作 / Shift management service implementation
 * @description_zh 班表 CRUD 與員工指派邏輯
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftScheduleRepository shiftScheduleRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Override
    public List<ShiftSchedule> getAllShifts() {
        return shiftScheduleRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null)
                .toList();
    }

    @Override
    public ShiftSchedule getShiftById(UUID id) {
        return shiftScheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "班表不存在 / Shift not found"));
    }

    @Override
    @Transactional
    public ShiftSchedule createShift(ShiftSchedule shift) {
        return shiftScheduleRepository.save(shift);
    }

    @Override
    @Transactional
    public ShiftSchedule updateShift(UUID id, ShiftSchedule updated) {
        ShiftSchedule shift = getShiftById(id);
        shift.setName(updated.getName());
        shift.setType(updated.getType());
        shift.setStartTime(updated.getStartTime());
        shift.setEndTime(updated.getEndTime());
        shift.setGraceMinutes(updated.getGraceMinutes());
        shift.setBreakStart(updated.getBreakStart());
        shift.setBreakEnd(updated.getBreakEnd());
        return shiftScheduleRepository.save(shift);
    }

    @Override
    @Transactional
    public void deleteShift(UUID id) {
        ShiftSchedule shift = getShiftById(id);
        shift.setDeletedAt(LocalDateTime.now());
        shiftScheduleRepository.save(shift);
    }

    @Override
    @Transactional
    public void assignShift(String employeeId, UUID shiftId, String effectiveDate, String endDate) {
        // 確保班表存在 / Verify shift exists
        getShiftById(shiftId);

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployeeId(employeeId);
        assignment.setShiftScheduleId(shiftId);
        assignment.setEffectiveDate(LocalDate.parse(effectiveDate));
        if (endDate != null) {
            assignment.setEndDate(LocalDate.parse(endDate));
        }
        shiftAssignmentRepository.save(assignment);
        log.info("📋 Assigned shift {} to employee {} from {}", shiftId, employeeId, effectiveDate);
    }
}
