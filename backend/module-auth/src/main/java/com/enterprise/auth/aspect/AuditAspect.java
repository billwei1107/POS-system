package com.enterprise.auth.aspect;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.security.SecurityUtils;
import com.enterprise.auth.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @file AuditAspect.java
 * @description 稽核註解切面 / Audit annotation aspect
 * @description_en Emits structured audit logs for annotated sensitive operations
 * @description_zh 對標註 @Auditable 的敏感操作輸出結構化稽核紀錄
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentRole();
        String action = auditable.action();
        String module = auditable.module();
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            record(module, action, method, userId, role, AuditLogService.STATUS_SUCCESS, null, start);
            log.info("audit_status=SUCCESS module={} action={} method={} user_id={} role={}",
                    module, action, method, userId, role);
            return result;
        } catch (Throwable ex) {
            record(module, action, method, userId, role, AuditLogService.STATUS_FAILED, ex.getMessage(), start);
            log.warn("audit_status=FAILED module={} action={} method={} user_id={} role={} error={}",
                    module, action, method, userId, role, ex.getMessage());
            throw ex;
        }
    }

    private void record(String module, String action, String method, String userId, String role,
                        String status, String errorMessage, long start) {
        try {
            auditLogService.record(
                    module,
                    action,
                    method,
                    parseUserId(userId),
                    role,
                    status,
                    errorMessage,
                    System.currentTimeMillis() - start
            );
        } catch (Exception ex) {
            log.warn("audit_persist_status=FAILED module={} action={} method={} error={}",
                    module, action, method, ex.getMessage());
        }
    }

    private UUID parseUserId(String userId) {
        try {
            return userId == null ? null : UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
