package com.enterprise.attendance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @file AttendanceRecordDTO.java
 * @description 打卡記錄回傳 / Attendance record response DTO
 */
@Data
@Builder
public class AttendanceRecordDTO {
    private String id;
    private String employeeId;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String clockInMethod;
    private String status;
    private Integer overtimeMinutes;
}
