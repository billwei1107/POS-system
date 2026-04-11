package com.enterprise.notification.service;

public interface WebSocketService {
    void sendUnreadCountUpdate(String userId, long unreadCount);
    void sendNewNotificationAlert(String userId, Object notificationPayload);
}
