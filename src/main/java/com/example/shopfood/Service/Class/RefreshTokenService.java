package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.RefreshToken;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Repository.RefreshTokenRepository;
import com.example.shopfood.Service.IRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Service
public class RefreshTokenService implements IRefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RefreshToken issue(Users user, HttpServletRequest request) {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(tokenValue);
        rt.setIssuedAt(new Date());
        rt.setExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationMs));
        if (request != null) {
            rt.setUserAgent(request.getHeader("User-Agent"));
            rt.setIpAddress(request.getRemoteAddr());
        }
        return refreshTokenRepository.save(rt);
    }

    @Override
    @Transactional
    public RefreshToken rotate(String oldToken, HttpServletRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));
        if (existing.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }
        if (existing.getExpiresAt().before(new Date())) {
            throw new RuntimeException("Refresh token đã hết hạn");
        }
        // rotate: revoke cái cũ, cấp cái mới
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return issue(existing.getUser(), request);
    }

    @Override
    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(Users user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Override
    public Users validateAndGetUser(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));
        if (rt.isRevoked() || rt.getExpiresAt().before(new Date())) {
            throw new RuntimeException("Refresh token đã hết hiệu lực");
        }
        return rt.getUser();
    }
}
