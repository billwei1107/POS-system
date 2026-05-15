package com.enterprise.auth.service;

import com.enterprise.auth.dto.AuditLogResponse;
import com.enterprise.auth.entity.AuditLog;
import com.enterprise.auth.repository.AuditLogRepository;
import com.enterprise.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file AuditLogService.java
 * @description 稽核紀錄服務 / Audit log service
 * @description_en Persists and queries sensitive operation audit records
 * @description_zh 負責寫入與查詢敏感操作稽核紀錄
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String module, String action, String methodName, UUID userId,
                       String roleCode, String status, String errorMessage, long durationMs) {
        AuditLog auditLog = new AuditLog();
        auditLog.setModule(trimToLength(module, 100));
        auditLog.setAction(trimToLength(action, 100));
        auditLog.setMethodName(trimToLength(methodName, 255));
        auditLog.setUserId(userId);
        auditLog.setRoleCode(trimToLength(roleCode, 50));
        auditLog.setStatus(status);
        auditLog.setErrorMessage(trimToLength(errorMessage, 500));
        auditLog.setDurationMs(Math.max(durationMs, 0));
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String module, String action, String status,
                                                 UUID userId, LocalDateTime from, LocalDateTime to,
                                                 Pageable pageable) {
        Pageable safePageable = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(Math.max(pageable.getPageSize(), 1), 100),
                pageable.getSort()
        );
        Specification<AuditLog> spec = buildFilter(module, action, status, userId, from, to);
        return PageResponse.of(auditLogRepository.findAll(spec, safePageable).map(AuditLogResponse::from));
    }

    private Specification<AuditLog> buildFilter(String module, String action, String status,
                                                UUID userId, LocalDateTime from, LocalDateTime to) {
        return Specification.where(equalsIfPresent("module", module))
                .and(equalsIfPresent("action", action))
                .and(equalsIfPresent("status", status))
                .and(userId == null ? null : (root, query, cb) -> cb.equal(root.get("userId"), userId))
                .and(from == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from))
                .and(to == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
    }

    private Specification<AuditLog> equalsIfPresent(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private String trimToLength(String value, int length) {
        if (value == null) {
            return null;
        }
        return value.length() <= length ? value : value.substring(0, length);
    }
}
