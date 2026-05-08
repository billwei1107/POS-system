/**
 * @file StaffSchedule.java
 * @description 排班計劃實體 / Staff schedule entity
 * @description_en Represents a planned work shift for an employee on a specific date
 * @description_zh 員工的排班計劃，包含計劃上班時段與實際班次關聯
 */
package com.enterprise.staff.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_schedules")
@Getter
@Setter
public class StaffSchedule extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "planned_start", nullable = false)
    private LocalTime plannedStart;

    @Column(name = "planned_end", nullable = false)
    private LocalTime plannedEnd;

    @Column(name = "actual_shift_id")
    private UUID actualShiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum ScheduleStatus {
        SCHEDULED, CONFIRMED, ABSENT, SWAPPED
    }
}
