package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @file AttendanceRecord.java
 * @description 打卡記錄實體 / Attendance record entity
 * @description_zh 記錄員工每日的上下班打卡時間、GPS 座標與出勤狀態
 */
@Entity
@Table(name = "att_attendance_records")
@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceRecord extends BaseEntity {

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "clock_in_location", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String clockInLocation;

    @Column(name = "clock_in_method", length = 10)
    private String clockInMethod = "GPS"; // GPS / WIFI / QR

    @Column(nullable = false, length = 20)
    private String status = "NORMAL"; // NORMAL / LATE / EARLY_LEAVE / ABSENT

    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes = 0;
}
