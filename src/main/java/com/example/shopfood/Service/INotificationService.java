package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Notification.NotificationRequest;

import java.util.List;

public interface INotificationService {
    void createNotification(NotificationRequest request);
    List<Notification> getNotificationsByUser(Users user);
    Notification markAsRead(Integer notificationId);
    void deleteNotification(Integer notificationId);
    void markAllAsRead(Users user);
}

