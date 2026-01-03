package com.example.shopfood.Service;

import com.example.shopfood.Model.DTO.NotificationDTO;
import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Notification.NotificationRequest;

import java.util.List;

public interface INotificationService {
    List<NotificationDTO> getMyNotifications(Users user);

    void createNotification(NotificationRequest request);

    NotificationDTO markAsRead(Integer notificationId, Users user);

    void markAllAsRead(Users user);

    void deleteNotification(Integer notificationId, Users user);

    List<NotificationDTO> getAllForAdmin();

}

