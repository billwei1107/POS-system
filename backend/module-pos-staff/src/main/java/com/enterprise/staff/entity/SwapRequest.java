/**
 * @file SwapRequest.java
 * @description 換班申請實體 / Shift swap request entity
 * @description_en Represents a request to swap scheduled shifts between two employees
 * @description_zh 員工換班申請，包含申請人、目標人、審批狀態
 */
package com.enterprise.staff.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pos_staff_swap_requests")
@Getter
@Setter
public class SwapRequest extends BaseEntity {

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "requester_sched", nullable = false)
    private UUID requesterScheduleId;

    @Column(name = "target_sched", nullable = false)
    private UUID targetScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SwapStatus status = SwapStatus.PENDING;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    public enum SwapStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
