/**
 * @file NotificationController.java
 * @description 通知 API 控制器 / Notification REST controller
 * @description_en REST endpoints for unread notifications, read state updates, and test pushes
 * @description_zh 提供未讀通知、已讀狀態更新與測試推播 API 端點
 */
package com.enterprise.notification.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.common.dto.ApiResponse;
import com.enterprise.common.security.SecurityUtils;
import com.enterprise.notification.dto.NotificationDTO;
import com.enterprise.notification.entity.Notification;
import com.enterprise.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @RequirePermission("system:notification:read")
    public ApiResponse<List<NotificationDTO>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<Notification> notis = notificationService.getUnreadNotifications(userId, PageRequest.of(page, size, Sort.by("sentAt").descending()));
        
        List<NotificationDTO> dtos = notis.stream().map(NotificationDTO::fromEntity).collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/count")
    @RequirePermission("system:notification:read")
    public ApiResponse<Long> getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @RequirePermission("system:notification:operate")
    @Auditable(module = "notification", action = "mark-read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    @RequirePermission("system:notification:operate")
    @Auditable(module = "notification", action = "mark-all-read")
    public ApiResponse<Void> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/test-push")
    @RequirePermission("system:notification:manage")
    @Auditable(module = "notification", action = "test-push")
    public ApiResponse<Void> testPush() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.send(userId, "SYS_TEST", "WEBSOCKET", "SYSTEM",
                Map.of("message", "WebSocket 推播測試成功！"));
        return ApiResponse.success(null);
    }
}
