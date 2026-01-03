package com.example.shopfood.Service.Class;

import com.example.shopfood.Repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationCleanupJob {

    @Autowired
    private NotificationRepository notificationRepository;

    // chạy mỗi ngày lúc 02:00 sáng
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupReadNotifications() {

        LocalDateTime expiredTime = LocalDateTime.now().minusDays(90);

        int deleted = notificationRepository.deleteReadOlderThan(expiredTime);

        System.out.println("Deleted " + deleted + " old read notifications");
    }
}

//@EnableScheduling
//@Component
//public class NotificationCleanupJob {
//
//    @Autowired
//    private NotificationRepository notificationRepository;
//
//    @Scheduled(fixedRate = 60000) // mỗi 1 phút
//    @Transactional
//    public void cleanup() {
//        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(2);
//        notificationRepository.deleteReadBefore(expireTime);
//    }
//}

