package com.enterprise.notification.service.impl;

import com.enterprise.notification.service.WebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendUnreadCountUpdate(String userId, long unreadCount) {
        // 發送給該用戶的專屬長連線 Topic
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications/count", unreadCount);
    }

    @Override
    public void sendNewNotificationAlert(String userId, Object notificationPayload) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications/new", notificationPayload);
    }
}
