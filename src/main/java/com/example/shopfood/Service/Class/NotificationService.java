package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.NotificationStatus;
import com.example.shopfood.Model.Entity.NotificationType;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Notification.NotificationRequest;
import com.example.shopfood.Repository.NotificationRepository;
import com.example.shopfood.Repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createNotification(NotificationRequest request) {

        if (request.getNotificationType() == NotificationType.PRIVATE) {
            // gửi cho 1 user cụ thể
            Users user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Notification n = new Notification();
            n.setNotificationType(NotificationType.PRIVATE);
            n.setTitle(request.getTitle());
            n.setDescription(request.getDescription());
            n.setRedirectUrl(request.getRedirectUrl());
            n.setStatus(NotificationStatus.UNREAD);
            n.setUser(user);

            notificationRepository.save(n);

        } else if (request.getNotificationType() == NotificationType.PUBLIC) {
            // gửi cho tất cả user
            List<Users> users = userRepository.findAll();
            for (Users user : users) {
                Notification n = new Notification();
                n.setNotificationType(NotificationType.PUBLIC);
                n.setTitle(request.getTitle());
                n.setDescription(request.getDescription());
                n.setRedirectUrl(request.getRedirectUrl());
                n.setStatus(NotificationStatus.UNREAD);
                n.setUser(user);
                notificationRepository.save(n);
            }
        }
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
