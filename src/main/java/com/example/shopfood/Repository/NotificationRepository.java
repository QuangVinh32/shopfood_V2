package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // Lấy tất cả notification của 1 user, có thể sắp xếp theo createdAt giảm dần
    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
}

