package com.example.shopfood.Controller;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Notification.NotificationRequest;
import com.example.shopfood.Service.INotificationService;
import com.example.shopfood.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByFullName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(notificationService.getNotificationsByUser(user));
    }


    @PostMapping
    public ResponseEntity<String> createNotification(@RequestBody NotificationRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.ok("Notification created successfully");
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByFullName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok("All notifications marked as read");
    }
}

