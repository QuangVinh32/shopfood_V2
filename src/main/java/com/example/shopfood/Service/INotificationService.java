package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.Users;

import java.util.List;

public interface INotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getNotificationsByUser(Users user);
    Notification markAsRead(Integer notificationId);
    void deleteNotification(Integer notificationId);
    void markAllAsRead(Users user);
}

