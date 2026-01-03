package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Notification;
import com.example.shopfood.Model.Entity.NotificationStatus;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // Lấy tất cả notification của 1 user, có thể sắp xếp theo createdAt giảm dần
    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByUserAndStatus(
            Users user,
            NotificationStatus status
    );


    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.status = 'READ'
        AND n.updatedAt < :expiredTime
    """)
    int deleteReadOlderThan(@Param("expiredTime") LocalDateTime expiredTime);

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = 'READ', n.updatedAt = :time
        WHERE n.user = :user AND n.status = 'UNREAD'
    """)
    int markAllRead(@Param("user") Users user,
                    @Param("time") LocalDateTime time);


    @Modifying
    @Query("""
    DELETE FROM Notification n
    WHERE n.status = 'READ'
    AND n.updatedAt < :time
""")
    void deleteReadBefore(@Param("time") LocalDateTime time);

}

