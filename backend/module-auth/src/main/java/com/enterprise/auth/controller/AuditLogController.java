package com.enterprise.auth.controller;

import com.enterprise.auth.dto.AuditLogResponse;
import com.enterprise.auth.service.AuditLogService;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file AuditLogController.java
 * @description 稽核紀錄查詢 API / Audit log query API
 * @description_en Provides protected paged search for sensitive operation audit logs
 * @description_zh 提供受保護的敏感操作稽核紀錄分頁查詢
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @RequirePermission("system:audit:read")
    public ApiResponse<PageResponse<AuditLogResponse>> search(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("occurredAt").descending()
        );
        return ApiResponse.success(auditLogService.search(module, action, status, userId, from, to, pageable));
    }
}
