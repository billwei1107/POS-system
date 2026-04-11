package com.enterprise.notification.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_notifications")
@Getter
@Setter
public class Notification extends BaseEntity {
    @Column(nullable = false, length = 36)
    private String userId;

    @Column(length = 36)
    private String templateId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String status = "UNREAD";

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column
    private LocalDateTime readAt;
}
