package com.example.shopfood.Service.Class;

import com.example.shopfood.Repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationCleanupJob.class);

    @Autowired
    private NotificationRepository notificationRepository;

    // Chạy 02:00 hàng ngày, xóa notification đã đọc cũ hơn 90 ngày
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupReadNotifications() {
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(90);
        int deleted = notificationRepository.deleteReadOlderThan(expiredTime);
        log.info("Notification cleanup: deleted {} old read notifications", deleted);
    }
}
