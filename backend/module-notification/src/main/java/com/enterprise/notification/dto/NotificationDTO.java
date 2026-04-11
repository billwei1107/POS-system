package com.enterprise.notification.dto;

import com.enterprise.notification.entity.Notification;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {
    private String id;
    private String title;
    private String content;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    
    public static NotificationDTO fromEntity(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setStatus(entity.getStatus());
        dto.setSentAt(entity.getSentAt());
        dto.setReadAt(entity.getReadAt());
        return dto;
    }
}
