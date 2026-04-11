package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file AttendanceCorrection.java
 * @description 補卡申請實體 / Attendance correction entity
 * @description_zh 員工補卡申請，可與審批流程模組串接
 */
@Entity
@Table(name = "att_attendance_corrections")
@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceCorrection extends BaseEntity {

    @Column(name = "record_id")
    private UUID recordId;

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "original_time")
    private LocalDateTime originalTime;

    @Column(name = "corrected_time", nullable = false)
    private LocalDateTime correctedTime;

    @Column(name = "correction_type", nullable = false, length = 20)
    private String correctionType = "CLOCK_IN"; // CLOCK_IN / CLOCK_OUT

    @Column(name = "workflow_instance_id", length = 36)
    private String workflowInstanceId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED
}
