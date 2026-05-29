package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IEmailService;
import com.example.shopfood.Service.IEmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService implements IEmailVerificationService {

    @Autowired private UserRepository userRepository;
    @Autowired private IEmailService emailService;
    @Autowired private BCryptPasswordEncoder encoder;

    @Value("${app.mail.verify-link-base}")
    private String verifyLinkBase;

    @Value("${app.mail.reset-link-base}")
    private String resetLinkBase;

    @Override
    @Transactional
    public void sendVerificationEmail(Users user) {
        if (user.isEmailVerified()) return;

        String token = UUID.randomUUID().toString().replace("-", "");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 24);
        user.setEmailVerifyToken(token);
        user.setEmailVerifyExpiresAt(c.getTime());
        userRepository.save(user);

        Map<String, Object> vars = new HashMap<>();
        vars.put("fullName", user.getFullName());
        vars.put("verifyLink", verifyLinkBase + "?token=" + token);
        emailService.sendHtml(user.getEmail(), "Xác thực email Shopfood", "verify-email", vars);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Users user = userRepository.findByEmailVerifyToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        if (user.getEmailVerifyExpiresAt() == null
                || user.getEmailVerifyExpiresAt().before(new Date())) {
            throw new RuntimeException("Token đã hết hạn");
        }
        user.setEmailVerified(true);
        user.setEmailVerifyToken(null);
        user.setEmailVerifyExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendResetPasswordEmail(String email) {
        Optional<Users> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) return; // im lặng → chống enumeration

        Users user = opt.get();
        String token = UUID.randomUUID().toString().replace("-", "");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiresAt(c.getTime());
        userRepository.save(user);

        Map<String, Object> vars = new HashMap<>();
        vars.put("fullName", user.getFullName());
        vars.put("resetLink", resetLinkBase + "?token=" + token);
        emailService.sendHtml(user.getEmail(), "Đặt lại mật khẩu Shopfood", "reset-password", vars);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Users user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));
        if (user.getResetPasswordExpiresAt() == null
                || user.getResetPasswordExpiresAt().before(new Date())) {
            throw new RuntimeException("Token đã hết hạn");
        }
        user.setPassword(encoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        userRepository.save(user);
    }
}
