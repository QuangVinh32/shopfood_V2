package com.example.shopfood.Config.JWT;
import com.alibaba.fastjson2.JSON;
import com.example.shopfood.Exception.AppException;
import com.example.shopfood.Model.DTO.LoginDTO;
import com.example.shopfood.Model.Entity.Role;
import com.example.shopfood.Model.Entity.Token;
import com.example.shopfood.Repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtils.class);
    private static final long EXPIRATION = 864000000L;

    // Key đủ dài (nên lưu ở biến môi trường khi deploy Railway)
    private static final String SECRET = "my-super-secret-key-which-is-at-least-64-characters-long-1234567890";
    private static final String PREFIX_TOKEN = "Bearer";
    private static final String AUTHORIZATION = "Authorization";
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private TokenRepository tokenRepository;

    public String createAccessToken(LoginDTO loginDTO) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION);
        String token = Jwts.builder()
                .setId(String.valueOf(loginDTO.getUserId()))
                .setSubject(loginDTO.getUsername())
                .setIssuedAt(new Date())
                .setIssuer(String.valueOf(loginDTO.getUserId()))
                .setExpiration(expirationDate)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .claim("authorities", loginDTO.getRole().name())
                .claim("loginId", loginDTO.getUserId())
                .claim("user-Agent", loginDTO.getUserAgent())
                .compact();

        Token tokenEntity = new Token();
        tokenEntity.setToken(token);
        tokenEntity.setExpiration(expirationDate);
        tokenEntity.setUserAgent(loginDTO.getUserAgent());
        this.tokenRepository.save(tokenEntity);

        return token;
    }

    public LoginDTO parseAccessToken(String token) {
        LoginDTO loginDto = new LoginDTO();
        if (!token.isEmpty()) {
            try {
                token = token.replace(PREFIX_TOKEN, "").trim();
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(SIGNING_KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String user = claims.getSubject();
                Role role = Role.valueOf(claims.get("authorities").toString());
                String userAgent = claims.get("user-Agent").toString();
                loginDto.setUsername(user);
                loginDto.setRole(role);
                loginDto.setUserAgent(userAgent);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return loginDto;
    }
    public boolean checkToken(String token, HttpServletResponse response, HttpServletRequest httpServletRequest) {
        try {
            if (!StringUtils.isBlank(token) && token.startsWith("Bearer")) {
                token = token.replace("Bearer", "").trim();
                Token tokenEntity = this.tokenRepository.findByToken(token);
                if (tokenEntity == null) {
                    this.responseJson(response, new AppException("Token không tồn tại", 401, httpServletRequest.getRequestURI()));
                    return false;
                } else if (tokenEntity.getExpiration().after(new Date(System.currentTimeMillis() + 864000000L))) {
                    this.responseJson(response, new AppException("Token hết hiệu lực", 401, httpServletRequest.getRequestURI()));
                    return false;
                } else {
                    return true;
                }
            } else {
                this.responseJson(response, new AppException("Token không hợp lệ", 401, httpServletRequest.getRequestURI()));
                return false;
            }
        } catch (Exception e) {
            this.responseJson(response, new AppException(e.getMessage(), 401, httpServletRequest.getRequestURI()));
            return false;
        }
    }

    private void responseJson(HttpServletResponse response, AppException appException) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(appException.getCode());

        try {
            response.getWriter().print(JSON.toJSONString(appException));
        } catch (IOException e) {
            log.debug(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}