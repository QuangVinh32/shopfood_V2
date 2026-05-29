package com.example.shopfood.Service.Class;

import com.example.shopfood.Repository.RefreshTokenRepository;
import com.example.shopfood.Repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Chạy 03:00 hàng ngày, dọn:
     *  - Token (access) đã hết hạn
     *  - RefreshToken đã hết hạn hoặc revoked
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        try {
            int accessDeleted = tokenRepository.deleteExpired();
            refreshTokenRepository.deleteExpiredOrRevoked();
            log.info("Token cleanup: {} access tokens deleted, refresh tokens cleaned", accessDeleted);
        } catch (Exception e) {
            log.error("Token cleanup failed", e);
        }
    }
}
