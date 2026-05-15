package com.enterprise.auth.service;

import com.enterprise.auth.entity.AuditLog;
import com.enterprise.auth.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @file AuditLogServiceTest.java
 * @description 稽核紀錄服務測試 / Audit log service tests
 * @description_en Verifies persisted audit records are normalized before saving
 * @description_zh 驗證稽核紀錄寫入前會完成欄位正規化
 */
class AuditLogServiceTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final AuditLogService auditLogService = new AuditLogService(auditLogRepository);

    @Test
    void recordTrimsLongFieldsAndSetsOccurredAt() {
        UUID userId = UUID.randomUUID();
        String longError = "x".repeat(600);

        auditLogService.record(
                "inventory-stock-take-extra-long-module-name",
                "complete",
                "StockTakeController.completeStockTake(..)",
                userId,
                "STORE_MANAGER",
                AuditLogService.STATUS_FAILED,
                longError,
                25
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertEquals("inventory-stock-take-extra-long-module-name", saved.getModule());
        assertEquals("complete", saved.getAction());
        assertEquals("StockTakeController.completeStockTake(..)", saved.getMethodName());
        assertEquals(userId, saved.getUserId());
        assertEquals("STORE_MANAGER", saved.getRoleCode());
        assertEquals(AuditLogService.STATUS_FAILED, saved.getStatus());
        assertEquals(500, saved.getErrorMessage().length());
        assertEquals(25, saved.getDurationMs());
        assertNotNull(saved.getOccurredAt());
    }

    @Test
    void recordNormalizesNegativeDuration() {
        auditLogService.record(
                "pos-refund",
                "create",
                "RefundController.createRefund(..)",
                null,
                null,
                AuditLogService.STATUS_SUCCESS,
                null,
                -1
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertTrue(captor.getValue().getDurationMs() >= 0);
    }
}
