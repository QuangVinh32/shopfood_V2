package com.example.shopfood.Utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class CookieUtils {

    @Value("${app.cookie.refresh.name:refresh_token}")
    private String refreshCookieName;

    @Value("${app.cookie.refresh.path:/api/auth}")
    private String refreshCookiePath;

    @Value("${app.cookie.refresh.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.cookie.refresh.same-site:Lax}")
    private String refreshCookieSameSite;

    @Value("${app.cookie.refresh.domain:}")
    private String refreshCookieDomain;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    public String getRefreshCookieName() {
        return refreshCookieName;
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .sameSite(refreshCookieSameSite)
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs));

        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            builder.domain(refreshCookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .sameSite(refreshCookieSameSite)
                .maxAge(0);

        if (refreshCookieDomain != null && !refreshCookieDomain.isBlank()) {
            builder.domain(refreshCookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public Optional<String> readRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie c : request.getCookies()) {
            if (refreshCookieName.equals(c.getName())) {
                return Optional.ofNullable(c.getValue()).filter(v -> !v.isBlank());
            }
        }
        return Optional.empty();
    }
}
