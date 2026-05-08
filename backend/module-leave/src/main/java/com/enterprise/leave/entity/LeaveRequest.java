/**
 * @file LeaveRequest.java
 * @description 請假申請實體 / Leave request entity
 * @description_en Represents an employee leave application with workflow integration
 * @description_zh 員工請假申請，整合審批流程
 */
package com.enterprise.leave.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
public class LeaveRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id", nullable = false)
    private UUID leaveTypeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_half", nullable = false, length = 20)
    private HalfDay startHalf = HalfDay.FULL;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_half", nullable = false, length = 20)
    private HalfDay endHalf = HalfDay.FULL;

    @Column(name = "total_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "delegate_id")
    private UUID delegateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    public enum HalfDay {
        FULL, MORNING, AFTERNOON
    }

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
