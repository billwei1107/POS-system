package com.enterprise.auth.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file AuditLog.java
 * @description 稽核紀錄實體 / Audit log entity
 * @description_en Stores immutable records for sensitive backend operations
 * @description_zh 儲存敏感後端操作的不可變稽核紀錄
 */
@Entity
@Table(name = "auth_audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String module;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "method_name", nullable = false, length = 255)
    private String methodName;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
