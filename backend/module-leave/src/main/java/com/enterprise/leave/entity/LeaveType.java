/**
 * @file LeaveType.java
 * @description 假別類型實體 / Leave type entity
 * @description_en Defines leave categories such as annual, sick, personal leave
 * @description_zh 定義假別分類，如特休、病假、事假等
 */
package com.enterprise.leave.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
public class LeaveType extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_type", nullable = false, length = 20)
    private PaidType paidType;

    @Column(name = "require_attachment", nullable = false)
    private boolean requireAttachment = false;

    @Column(name = "max_days_per_year", precision = 5, scale = 1)
    private BigDecimal maxDaysPerYear;

    public enum PaidType {
        PAID, UNPAID, HALF_PAY
    }
}
