package com.enterprise.attendance.service;

import com.enterprise.attendance.dto.AttendanceRecordDTO;
import com.enterprise.attendance.dto.ClockInRequest;
import com.enterprise.attendance.dto.CorrectionRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * @file AttendanceService.java
 * @description 打卡考勤服務介面 / Attendance service interface
 */
public interface AttendanceService {

    AttendanceRecordDTO clockIn(ClockInRequest request);

    AttendanceRecordDTO clockOut(String employeeId);

    AttendanceRecordDTO getDailyRecord(String employeeId, LocalDate date);

    List<AttendanceRecordDTO> getMonthlyRecords(String employeeId, int year, int month);

    void submitCorrection(CorrectionRequest request);
}
