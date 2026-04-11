package com.enterprise.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file BaseEntity.java
 * @description 所有 Entity 的共用基底類別 / Base entity class for all domain models
 * @description_en Defines commons fields such as UUID, created_at, updated_at, deleted_at
 * @description_zh 定義所有實體的共用屬性：主鍵、建立時間、更新時間、刪除時間
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 軟刪除欄位，配合 @SQLDelete 與 @Where 達成
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
