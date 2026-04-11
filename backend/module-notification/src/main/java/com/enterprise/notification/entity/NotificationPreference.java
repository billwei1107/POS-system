package com.enterprise.notification.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_notification_preferences")
@Getter
@Setter
public class NotificationPreference extends BaseEntity {
    @Column(nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 50)
    private String moduleSource;
}
