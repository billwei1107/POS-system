package com.enterprise.notification.controller;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationDTO>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<Notification> notis = notificationService.getUnreadNotifications(userId, PageRequest.of(page, size, Sort.by("sentAt").descending()));
        
        List<NotificationDTO> dtos = notis.stream().map(NotificationDTO::fromEntity).collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/count")
    public ApiResponse<Long> getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
}
