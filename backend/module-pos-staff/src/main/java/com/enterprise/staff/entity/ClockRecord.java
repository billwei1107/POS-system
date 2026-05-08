/**
 * @file ClockRecord.java
 * @description 打卡記錄實體 / Clock in/out record entity
 * @description_en Records employee clock in, clock out, and break events
 * @description_zh 員工打卡記錄，包含上班、下班、休息開始/結束
 */
package com.enterprise.staff.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_clock_records")
@Getter
@Setter
public class ClockRecord extends BaseEntity {

    @Column(name = "shift_id", nullable = false)
    private UUID shiftId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "clock_type", nullable = false, length = 10)
    private ClockType clockType;

    @Column(name = "clocked_at", nullable = false)
    private Instant clockedAt;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum ClockType {
        IN, OUT, BREAK_START, BREAK_END
    }
}
