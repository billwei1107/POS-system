package com.enterprise.auth.repository;

import com.enterprise.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @file AuditLogRepository.java
 * @description 稽核紀錄資料存取 / Audit log repository
 * @description_en Provides paged and filtered access to persisted audit logs
 * @description_zh 提供稽核紀錄的分頁與條件查詢能力
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
}
