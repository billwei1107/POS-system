package com.enterprise.notification.listener;

import com.enterprise.common.event.SystemNotificationEvent;
import com.enterprise.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AllModuleEventListener {

    private final NotificationService notificationService;

    public AllModuleEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 監聽從系統任何角落 (只要引入 module-common) 拋出的 SystemNotificationEvent，並送進通知引擎
     */
    @EventListener
    public void handleSystemNotificationEvent(SystemNotificationEvent event) {
        log.info("🔔 [通知中心總機] 收到來自模組 '{}' 針對用戶 '{}' 的訊息發送請求 (樣板: {})", 
                event.getModuleSource(), event.getUserId(), event.getTemplateCode());
        
        notificationService.send(
                event.getUserId(),
                event.getTemplateCode(),
                event.getChannel(),
                event.getModuleSource(),
                event.getVariables()
        );
    }
}
