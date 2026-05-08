/**
 * @file LeaveBalance.java
 * @description 員工餘假實體 / Employee leave balance entity
 * @description_en Tracks total, used, and remaining days per employee per leave type per year
 * @description_zh 追蹤每位員工每年各假別的總額、已用、剩餘天數
 */
package com.enterprise.leave.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_balances")
@Getter
@Setter
public class LeaveBalance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(nullable = false)
    private int year;

    @Column(name = "total_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalDays = BigDecimal.ZERO;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "remaining_days", nullable = false, precision = 5, scale = 1)
    private BigDecimal remainingDays = BigDecimal.ZERO;
}
