package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @file ShiftAssignment.java
 * @description 班表指派實體 / Shift assignment entity
 * @description_zh 將員工指派至特定班表，具有生效與結束日期
 */
@Entity
@Table(name = "att_shift_assignments")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftAssignment extends BaseEntity {

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(name = "shift_schedule_id", nullable = false)
    private UUID shiftScheduleId;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
