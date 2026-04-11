package com.enterprise.attendance.service;

import com.enterprise.attendance.entity.ShiftSchedule;

import java.util.List;
import java.util.UUID;

/**
 * @file ShiftService.java
 * @description 班表管理服務介面 / Shift management service interface
 */
public interface ShiftService {

    List<ShiftSchedule> getAllShifts();

    ShiftSchedule getShiftById(UUID id);

    ShiftSchedule createShift(ShiftSchedule shift);

    ShiftSchedule updateShift(UUID id, ShiftSchedule shift);

    void deleteShift(UUID id);

    void assignShift(String employeeId, UUID shiftId, String effectiveDate, String endDate);
}
