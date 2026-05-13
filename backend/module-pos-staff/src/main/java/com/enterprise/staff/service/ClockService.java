/**
 * @file ClockService.java
 * @description 打卡服務 / Clock in/out service
 * @description_en Handles employee clock events including break start/end
 * @description_zh 管理員工打卡事件，含休息開始/結束
 */
package com.enterprise.staff.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.staff.entity.ClockRecord;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.repository.ClockRecordRepository;
import com.enterprise.staff.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClockService {

    private final ClockRecordRepository clockRecordRepository;
    private final StaffShiftRepository shiftRepository;

    // ========================================
    // 打卡（含休息開始/結束）/ Clock event (including break)
    // ========================================
    @Transactional
    public ClockRecord clock(UUID shiftId, ClockRecord.ClockType clockType, UUID terminalId, String notes) {
        StaffShift shift = shiftRepository.findById(shiftId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("SHIFT_NOT_FOUND: Shift not found: " + shiftId));

        if (shift.getStatus() != StaffShift.ShiftStatus.OPEN) {
            throw new BusinessException("SHIFT_NOT_OPEN: Cannot clock on a non-open shift");
        }

        ClockRecord record = new ClockRecord();
        record.setShiftId(shiftId);
        record.setEmployeeId(shift.getEmployeeId());
        record.setStoreId(shift.getStoreId());
        record.setClockType(clockType);
        record.setClockedAt(Instant.now());
        record.setTerminalId(terminalId);
        record.setNotes(notes);

        log.info("Clock {} recorded for shift {}", clockType, shiftId);
        return clockRecordRepository.save(record);
    }

    // ========================================
    // 查詢班次打卡記錄 / List clock records for shift
    // ========================================
    @Transactional(readOnly = true)
    public List<ClockRecord> listByShift(UUID shiftId) {
        return clockRecordRepository.findByShiftId(shiftId);
    }
}
