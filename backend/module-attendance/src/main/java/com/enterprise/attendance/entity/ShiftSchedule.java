package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * @file ShiftSchedule.java
 * @description 班表定義實體 / Shift schedule definition entity
 * @description_zh 定義上班時間、下班時間、遲到寬限分鐘數等班務規則
 */
@Entity
@Table(name = "att_shift_schedules")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftSchedule extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String type = "FIXED"; // FIXED / FLEXIBLE / ROTATING

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes = 5;

    @Column(name = "break_start")
    private LocalTime breakStart;

    @Column(name = "break_end")
    private LocalTime breakEnd;
}
