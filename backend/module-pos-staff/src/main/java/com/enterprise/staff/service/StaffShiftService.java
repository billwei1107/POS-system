/**
 * @file StaffShiftService.java
 * @description 班次管理服務 / Staff shift management service
 * @description_en Manages shift open/close, blind close, and handover workflows
 * @description_zh 管理班次開啟/關閉、盲點結算、交接班流程
 */
package com.enterprise.staff.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.staff.entity.ClockRecord;
import com.enterprise.staff.entity.ShiftHandover;
import com.enterprise.staff.entity.StaffShift;
import com.enterprise.staff.repository.ClockRecordRepository;
import com.enterprise.staff.repository.ShiftHandoverRepository;
import com.enterprise.staff.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffShiftService {

    private final StaffShiftRepository shiftRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final ShiftHandoverRepository handoverRepository;

    private static final DateTimeFormatter SHIFT_NO_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Taipei"));

    // ========================================
    // 開班 / Open shift
    // ========================================
    @Transactional
    public StaffShift openShift(UUID storeId, UUID employeeId, UUID terminalId, BigDecimal openingCash) {
        shiftRepository.findOpenByEmployeeId(employeeId).ifPresent(existing -> {
            throw new BusinessException("SHIFT_ALREADY_OPEN: Employee already has an open shift: " + existing.getShiftNo());
        });

        StaffShift shift = new StaffShift();
        shift.setStoreId(storeId);
        shift.setEmployeeId(employeeId);
        shift.setTerminalId(terminalId);
        shift.setShiftNo("SH" + SHIFT_NO_FMT.format(Instant.now()) + employeeId.toString().substring(0, 4).toUpperCase());
        shift.setOpenedAt(Instant.now());
        shift.setOpeningCash(openingCash != null ? openingCash : BigDecimal.ZERO);
        shift.setStatus(StaffShift.ShiftStatus.OPEN);
        StaffShift saved = shiftRepository.save(shift);

        // ========================================
        // 記錄打卡 IN / Record clock-in
        // ========================================
        ClockRecord clockIn = new ClockRecord();
        clockIn.setShiftId(saved.getId());
        clockIn.setEmployeeId(employeeId);
        clockIn.setStoreId(storeId);
        clockIn.setClockType(ClockRecord.ClockType.IN);
        clockIn.setClockedAt(Instant.now());
        clockIn.setTerminalId(terminalId);
        clockRecordRepository.save(clockIn);

        log.info("Shift opened: {} for employee {}", saved.getShiftNo(), employeeId);
        return saved;
    }

    // ========================================
    // 一般關班 / Normal close shift
    // ========================================
    @Transactional
    public StaffShift closeShift(UUID shiftId, BigDecimal closingCash, String notes) {
        StaffShift shift = findOrThrow(shiftId);
        if (shift.getStatus() != StaffShift.ShiftStatus.OPEN) {
            throw new BusinessException("SHIFT_NOT_OPEN: Shift is not in OPEN status");
        }
        BigDecimal expected = shift.getOpeningCash().add(shift.getNetSales());
        shift.setClosingCash(closingCash);
        shift.setExpectedCash(expected);
        shift.setCashVariance(closingCash.subtract(expected));
        shift.setClosedAt(Instant.now());
        shift.setStatus(StaffShift.ShiftStatus.CLOSED);
        shift.setNotes(notes);

        ClockRecord clockOut = new ClockRecord();
        clockOut.setShiftId(shiftId);
        clockOut.setEmployeeId(shift.getEmployeeId());
        clockOut.setStoreId(shift.getStoreId());
        clockOut.setClockType(ClockRecord.ClockType.OUT);
        clockOut.setClockedAt(Instant.now());
        clockRecordRepository.save(clockOut);

        log.info("Shift closed: {} variance={}", shift.getShiftNo(), shift.getCashVariance());
        return shiftRepository.save(shift);
    }

    // ========================================
    // 盲點結算（Blind Close）/ Blind close without cash count
    // ========================================
    @Transactional
    public StaffShift blindCloseShift(UUID shiftId, String notes) {
        StaffShift shift = findOrThrow(shiftId);
        if (shift.getStatus() != StaffShift.ShiftStatus.OPEN) {
            throw new BusinessException("SHIFT_NOT_OPEN: Shift is not in OPEN status");
        }
        shift.setClosedAt(Instant.now());
        shift.setStatus(StaffShift.ShiftStatus.BLIND_CLOSED);
        shift.setNotes(notes);
        log.info("Shift blind-closed: {}", shift.getShiftNo());
        return shiftRepository.save(shift);
    }

    // ========================================
    // 建立交接班記錄 / Create handover record
    // ========================================
    @Transactional
    public ShiftHandover createHandover(UUID fromShiftId, UUID toShiftId, BigDecimal cashCounted, String notes, UUID confirmedBy) {
        StaffShift fromShift = findOrThrow(fromShiftId);
        BigDecimal expected = fromShift.getOpeningCash().add(fromShift.getNetSales());

        ShiftHandover handover = new ShiftHandover();
        handover.setFromShiftId(fromShiftId);
        handover.setToShiftId(toShiftId);
        handover.setStoreId(fromShift.getStoreId());
        handover.setHandoverAt(Instant.now());
        handover.setCashCounted(cashCounted);
        handover.setCashExpected(expected);
        handover.setCashVariance(cashCounted.subtract(expected));
        handover.setNotes(notes);
        handover.setConfirmedBy(confirmedBy);
        handover.setConfirmedAt(Instant.now());
        return handoverRepository.save(handover);
    }

    // ========================================
    // 查詢門店開放班次 / List open shifts
    // ========================================
    @Transactional(readOnly = true)
    public List<StaffShift> listOpenShifts(UUID storeId) {
        return shiftRepository.findOpenByStoreId(storeId);
    }

    // ========================================
    // 查詢班次門店 / Find shift store
    // ========================================
    @Transactional(readOnly = true)
    public UUID findStoreId(UUID shiftId) {
        return findOrThrow(shiftId).getStoreId();
    }

    // ========================================
    // 累計銷售到班次 / Accumulate sale to shift
    // ========================================
    @Transactional
    public void accumulateSale(UUID shiftId, BigDecimal amount, BigDecimal tax, BigDecimal discount) {
        StaffShift shift = findOrThrow(shiftId);
        shift.addSale(amount, tax, discount);
        shiftRepository.save(shift);
    }

    // ========================================
    // 累計退款到班次 / Accumulate refund to shift
    // ========================================
    @Transactional
    public void accumulateRefund(UUID shiftId, BigDecimal amount) {
        StaffShift shift = findOrThrow(shiftId);
        shift.addRefund(amount);
        shiftRepository.save(shift);
    }

    private StaffShift findOrThrow(UUID shiftId) {
        return shiftRepository.findById(shiftId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("SHIFT_NOT_FOUND: Shift not found: " + shiftId));
    }
}
