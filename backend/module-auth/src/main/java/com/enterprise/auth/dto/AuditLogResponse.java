package com.enterprise.auth.dto;

import com.enterprise.auth.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file AuditLogResponse.java
 * @description 稽核紀錄回應 DTO / Audit log response DTO
 * @description_en Serializes audit log records without exposing persistence internals
 * @description_zh 將稽核紀錄轉為 API 回應格式，避免直接暴露持久化細節
 */
public record AuditLogResponse(
        UUID id,
        String module,
        String action,
        String methodName,
        UUID userId,
        String roleCode,
        String status,
        String errorMessage,
        long durationMs,
        LocalDateTime occurredAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getModule(),
                log.getAction(),
                log.getMethodName(),
                log.getUserId(),
                log.getRoleCode(),
                log.getStatus(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getOccurredAt()
        );
    }
}
