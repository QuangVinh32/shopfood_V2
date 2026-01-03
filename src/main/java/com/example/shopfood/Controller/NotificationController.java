package com.example.shopfood.Controller;
import com.example.shopfood.Model.DTO.NotificationDTO;
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

    private Users currentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByFullName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(currentUser())
        );
    }

    @PostMapping
    public ResponseEntity<?> createNotification(
            @RequestBody NotificationRequest request
    ) {
        notificationService.createNotification(request);
        return ResponseEntity.ok("Created");
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id, currentUser())
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead(currentUser());
        return ResponseEntity.ok("All read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id
    ) {
        notificationService.deleteNotification(id, currentUser());
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTO>> adminGetAll() {
        return ResponseEntity.ok(
                notificationService.getAllForAdmin()
        );
    }
}


