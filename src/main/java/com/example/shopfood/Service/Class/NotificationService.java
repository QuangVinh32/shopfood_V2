package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.NotificationStatus;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Repository.NotificationRepository;
import com.example.shopfood.Service.INotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsByUser(Users user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Notification markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void markAllAsRead(Users user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : notifications) {
            n.setStatus(NotificationStatus.READ);
        }
        notificationRepository.saveAll(notifications);
    }
}
