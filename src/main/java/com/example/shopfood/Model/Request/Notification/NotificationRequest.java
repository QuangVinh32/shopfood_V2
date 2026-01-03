package com.example.shopfood.Model.Request.Notification;

import com.example.shopfood.Model.Entity.NotificationType;
import lombok.Data;

@Data
public class NotificationRequest {
    private NotificationType notificationType; // USER, SYSTEM, ORDER, etc
    private String title;
    private String description;
    private String redirectUrl;
    private Integer userId; // chỉ dùng khi type = USER
}
