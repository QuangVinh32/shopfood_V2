package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.DTO.NotificationDTO;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // ================= GET =================
    @Override
    public List<NotificationDTO> getMyNotifications(Users user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::new)
                .toList();
    }

    // ================= CREATE =================
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

        } else if (request.getNotificationType() == NotificationType.ALL) {
            // gửi cho tất cả user
            List<Users> users = userRepository.findAll();
            for (Users user : users) {
                Notification n = new Notification();
                n.setNotificationType(NotificationType.ALL);
                n.setTitle(request.getTitle());
                n.setDescription(request.getDescription());
                n.setRedirectUrl(request.getRedirectUrl());
                n.setStatus(NotificationStatus.UNREAD);
                n.setUser(user);
                notificationRepository.save(n);
            }
        }
    }

    private void saveNotification(Users user, NotificationRequest request) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(request.getTitle());
        n.setDescription(request.getDescription());
        n.setNotificationType(request.getNotificationType());
        n.setRedirectUrl(request.getRedirectUrl());
        n.setStatus(NotificationStatus.UNREAD);

        notificationRepository.save(n);
    }

    // ================= READ ONE =================
    @Override
    public NotificationDTO markAsRead(Integer notificationId, Users user) {

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification không tồn tại"));

        // 🔥 CHECK OWNER
        if (!n.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền thao tác notification này");
        }

        n.setStatus(NotificationStatus.READ);
        n.setUpdatedAt(LocalDateTime.now()); // thời điểm đọc
        return new NotificationDTO(notificationRepository.save(n));
    }

    // ================= READ ALL =================
    @Override
    @Transactional
    public void markAllAsRead(Users user) {
        notificationRepository.markAllRead(user, LocalDateTime.now());
    }


    // ================= DELETE =================
    @Override
    public void deleteNotification(Integer notificationId, Users user) {

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification không tồn tại"));

        // 🔥 CHECK OWNER
        if (!n.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền xóa notification này");
        }

        notificationRepository.delete(n);
    }

    @Override
    public List<NotificationDTO> getAllForAdmin() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(NotificationDTO::new)
                .toList();
    }
}
