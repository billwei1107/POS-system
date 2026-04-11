package com.enterprise.notification.service.impl;

import com.enterprise.notification.entity.Notification;
import com.enterprise.notification.entity.NotificationPreference;
import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.repository.NotificationPreferenceRepository;
import com.enterprise.notification.repository.NotificationRepository;
import com.enterprise.notification.repository.NotificationTemplateRepository;
import com.enterprise.notification.service.NotificationService;
import com.enterprise.notification.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final WebSocketService webSocketService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationTemplateRepository templateRepository,
                                   NotificationPreferenceRepository preferenceRepository,
                                   WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.preferenceRepository = preferenceRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    @Transactional
    public void send(String userId, String templateCode, String channel, String moduleSource, Map<String, Object> variables) {
        // 1. 檢查使用者個人偏好設定，若關閉此通道則直接棄置
        NotificationPreference pref = preferenceRepository.findByUserIdAndChannelAndModuleSource(userId, channel, moduleSource).orElse(null);
        if (pref != null && !pref.getEnabled()) {
             log.info("🚫 User {} disabled notifications for channel {} and source {}", userId, channel, moduleSource);
             return;
        }

        // 2. 獲取通知樣板 (防呆回退處理)
        NotificationTemplate template = templateRepository.findByCode(templateCode)
                .orElseGet(() -> {
                    log.warn("⚠️ Template '{}' not found in database, using system fallback.", templateCode);
                    NotificationTemplate t = new NotificationTemplate();
                    t.setId(UUID.randomUUID());
                    t.setSubject("SYSTEM BCAST: " + templateCode);
                    t.setBodyTemplate("你有新的系統通知");
                    return t;
                });

        // 3. 變數映射與文本替換
        String content = template.getBodyTemplate();
        String title = template.getSubject();
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String target = "{{" + entry.getKey() + "}}";
                String val = String.valueOf(entry.getValue());
                content = content.replace(target, val);
                if(title != null) title = title.replace(target, val);
            }
        }

        // 4. 存入資料庫作歷史查詢
        Notification noti = new Notification();
        noti.setUserId(userId);
        noti.setTemplateId(template.getId() != null ? template.getId().toString() : null);
        noti.setChannel(channel);
        noti.setTitle(title);
        noti.setContent(content);
        noti.setStatus("UNREAD");
        noti.setSentAt(LocalDateTime.now());
        
        notificationRepository.save(noti);

        // 5. 若通道為 WebSocket 或是 System，觸發 STOMP 直接推播給用戶 Browser
        if ("WEBSOCKET".equalsIgnoreCase(channel) || "SYSTEM".equalsIgnoreCase(channel)) {
            long unreadCount = notificationRepository.countByUserIdAndStatus(userId, "UNREAD");
            webSocketService.sendUnreadCountUpdate(userId, unreadCount);
            webSocketService.sendNewNotificationAlert(userId, noti);
        }
        
        // 6. Mock SMTP Email 等外部通道
        if ("EMAIL".equalsIgnoreCase(channel) || "LINE".equalsIgnoreCase(channel)) {
            log.info("✉️ [MOCK {} SENDER] 類比寄信成功 -> User: {}, Subject: {}", channel, userId, noti.getTitle());
        }
    }

    @Override
    public Page<Notification> getUnreadNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndStatus(userId, "UNREAD", pageable);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "UNREAD");
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setStatus("READ");
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
