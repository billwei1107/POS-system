/**
 * @file OfflineQueueEntry.java
 * @description 離線作業佇列實體 / Offline operation queue entity
 * @description_en Queues terminal operations for sync when connectivity is restored
 * @description_zh 在終端機離線期間緩衝操作，連線恢復後批次同步
 */
package com.enterprise.core.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_offline_queue")
@SQLDelete(sql = "UPDATE pos_offline_queue SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class OfflineQueueEntry extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "terminal_id", nullable = false)
    private UUID terminalId;

    @Column(name = "operation_type", nullable = false, length = 30)
    private String operationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum QueueStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
