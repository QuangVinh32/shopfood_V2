package com.example.shopfood.Model.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    private Long id;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 255)
    private String redirectUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
