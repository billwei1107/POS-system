/**
 * @file LeavePolicy.java
 * @description 假別政策實體（年資對應額度）/ Leave policy entity
 * @description_en Maps service years to annual leave quota per leave type
 * @description_zh 定義不同年資對應的年度假別額度
 */
package com.enterprise.leave.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_policies")
@Getter
@Setter
public class LeavePolicy extends BaseEntity {

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "min_service_years", nullable = false)
    private int minServiceYears = 0;

    @Column(name = "max_service_years")
    private Integer maxServiceYears;

    @Column(name = "annual_quota", nullable = false, precision = 5, scale = 1)
    private BigDecimal annualQuota;
}
