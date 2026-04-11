package com.enterprise.notification.service;

import com.enterprise.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationService {
    void send(String userId, String templateCode, String channel, String moduleSource, Map<String, Object> variables);
    Page<Notification> getUnreadNotifications(String userId, Pageable pageable);
    long getUnreadCount(String userId);
    void markAsRead(String notificationId);
    void markAllAsRead(String userId);
}
