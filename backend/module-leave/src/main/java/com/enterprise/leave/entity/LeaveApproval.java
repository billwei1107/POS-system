/**
 * @file LeaveApproval.java
 * @description 請假審批記錄實體 / Leave approval record entity
 * @description_en Audit trail of approval actions on leave requests
 * @description_zh 請假申請的審批動作稽核記錄
 */
package com.enterprise.leave.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leave_approvals")
@Getter
@Setter
public class LeaveApproval extends BaseEntity {

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "approver_id", nullable = false)
    private UUID approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "operated_at", nullable = false)
    private Instant operatedAt;

    public enum ApprovalAction {
        APPROVE, REJECT, FORWARD
    }
}
