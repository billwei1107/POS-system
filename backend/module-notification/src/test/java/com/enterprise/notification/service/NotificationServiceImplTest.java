/**
 * @file NotificationServiceImplTest.java
 * @description 通知服務資料範圍測試 / Notification service data-scope tests
 * @description_en Verifies read-state updates cannot modify another user's notification
 * @description_zh 驗證通知已讀操作不可修改其他使用者的通知
 */
package com.enterprise.notification.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.notification.entity.Notification;
import com.enterprise.notification.repository.NotificationPreferenceRepository;
import com.enterprise.notification.repository.NotificationRepository;
import com.enterprise.notification.repository.NotificationTemplateRepository;
import com.enterprise.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationTemplateRepository templateRepository;
    @Mock private NotificationPreferenceRepository preferenceRepository;
    @Mock private WebSocketService webSocketService;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                templateRepository,
                preferenceRepository,
                webSocketService
        );
    }

    @Test
    void markAsReadRejectsOtherUsersNotification() {
        Notification notification = new Notification();
        notification.setUserId("user-2");
        when(notificationRepository.findById("notification-1")).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead("notification-1", "user-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Notification data scope denied");
    }

    @Test
    void markAsReadUpdatesOwnedNotification() {
        Notification notification = new Notification();
        notification.setUserId("user-1");
        notification.setStatus("UNREAD");
        when(notificationRepository.findById("notification-1")).thenReturn(Optional.of(notification));

        notificationService.markAsRead("notification-1", "user-1");

        verify(notificationRepository).save(notification);
    }
}
