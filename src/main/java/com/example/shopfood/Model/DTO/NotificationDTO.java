package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Integer notificationId;
    private String title;
    private String description;
    private String type;
    private String status; // READ / UNREAD
    private String redirectUrl;
    private LocalDateTime createdAt;

    public NotificationDTO(Notification n) {
        this.notificationId = n.getNotificationId();
        this.title = n.getTitle();
        this.description = n.getDescription();
        this.type = n.getNotificationType().name();
        this.status = n.getStatus().name();
        this.redirectUrl = n.getRedirectUrl();
        this.createdAt = n.getCreatedAt();
    }
}
