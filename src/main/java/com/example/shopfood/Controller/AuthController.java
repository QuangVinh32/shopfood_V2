package com.example.shopfood.Controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.example.shopfood.Config.JWT.JwtTokenUtils;
import com.example.shopfood.Exception.AppException;
import com.example.shopfood.Exception.ErrorResponseBase;
import com.example.shopfood.Model.DTO.LoginDTO;
import com.example.shopfood.Model.DTO.TokenPairDTO;
import com.example.shopfood.Model.Entity.RefreshToken;
import com.example.shopfood.Model.Entity.Role;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.User.LoginRequest;
import com.example.shopfood.Model.Request.User.RefreshRequest;
import com.example.shopfood.Model.Request.User.UserRequest;
import com.example.shopfood.Repository.TokenRepository;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IEmailVerificationService;
import com.example.shopfood.Service.IFileService;
import com.example.shopfood.Service.IRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api"})
@Validated
public class AuthController {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 10;

    @Autowired private JwtTokenUtils jwtTokenUtils;
    @Autowired private UserRepository repository;
    @Autowired private IFileService fileService;
    @Autowired private BCryptPasswordEncoder encoder;
    @Autowired private HttpServletRequest httpServletRequest;
    @Autowired private IRefreshTokenService refreshTokenService;
    @Autowired private IEmailVerificationService emailVerificationService;
    @Autowired private TokenRepository tokenRepository;

    @Value("${app.public.base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Value("${app.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    private final ConcurrentMap<String, Integer> loginAttemptMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalDateTime> lockoutMap = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public LoginDTO loginJWT(@RequestBody @Valid LoginRequest request) {
        String username = request.getUsername();
        String ip = httpServletRequest.getRemoteAddr();
        String key = (username == null ? "" : username) + ":" + ip;

        LocalDateTime lockoutTime = lockoutMap.get(key);
        if (lockoutTime != null) {
            if (lockoutTime.isAfter(LocalDateTime.now())) {
                throw new AppException(ErrorResponseBase.Login_locked);
            }
            lockoutMap.remove(key);
            loginAttemptMap.remove(key);
        }

        Optional<Users> userOptional = repository.findByUsername(username);
        boolean credentialValid = userOptional.isPresent()
                && encoder.matches(request.getPassword(), userOptional.get().getPassword());

        if (!credentialValid) {
            int attempts = loginAttemptMap.merge(key, 1, Integer::sum);
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                lockoutMap.put(key, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
                throw new AppException(ErrorResponseBase.Login_locked);
            }
            throw new AppException(ErrorResponseBase.INVALID_CREDENTIAL);
        }

        Users user = userOptional.get();
        if (!user.isEnabled()) {
            throw new AppException(ErrorResponseBase.ACCOUNT_DISABLED);
        }

        loginAttemptMap.remove(key);
        lockoutMap.remove(key);

        LoginDTO loginDTO = new LoginDTO();
        BeanUtils.copyProperties(user, loginDTO);

        if (user.getImage() != null) {
            String fileName = Paths.get(user.getImage()).getFileName().toString();
            loginDTO.setImage(publicBaseUrl + "/files/image/" + fileName);
        }

        loginDTO.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        loginDTO.setToken(jwtTokenUtils.createAccessToken(loginDTO));

        // Cấp refresh token + trả ra trong response
        RefreshToken rt = refreshTokenService.issue(user, httpServletRequest);
        loginDTO.setRefreshToken(rt.getToken());

        return loginDTO;
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenPairDTO> refresh(@RequestBody @Valid RefreshRequest req) {
        RefreshToken newRt = refreshTokenService.rotate(req.getRefreshToken(), httpServletRequest);

        // Tạo access token mới từ user của refresh
        Users user = newRt.getUser();
        LoginDTO loginDTO = new LoginDTO();
        BeanUtils.copyProperties(user, loginDTO);
        loginDTO.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        String accessToken = jwtTokenUtils.createAccessToken(loginDTO);

        return ResponseEntity.ok(new TokenPairDTO(accessToken, newRt.getToken(), accessTokenExpirationMs));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid RefreshRequest req,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Revoke refresh token
        refreshTokenService.revoke(req.getRefreshToken());

        // Xóa access token khỏi DB → JwtRequestFilter sẽ reject các request kế tiếp
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7).trim();
            tokenRepository.deleteByTokenValue(accessToken);
        }
        return ResponseEntity.ok("Đã đăng xuất");
    }

    @PostMapping({"/register"})
    public ResponseEntity<?> registerUser(@Valid @ModelAttribute UserRequest userRequest) throws IOException {
        if (repository.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorResponseBase.USERNAME_EXISTS);
        }
        if (repository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorResponseBase.DOUBLE_EMAIL_EX);
        }
        if (repository.existsByPhone(userRequest.getPhone())) {
            throw new AppException(ErrorResponseBase.DUPLICATE_PHONE);
        }

        Users newUser = new Users();
        newUser.setAddress(userRequest.getAddress());
        newUser.setEmail(userRequest.getEmail());
        newUser.setPassword(encoder.encode(userRequest.getPassword()));
        newUser.setUsername(userRequest.getUsername());
        newUser.setPhone(userRequest.getPhone());
        String imagePath = fileService.uploadImage(userRequest.getImage());
        newUser.setImage(imagePath);
        newUser.setRole(Role.USER);
        newUser.setFullName(userRequest.getFullName());
        newUser.setEnabled(true);
        newUser.setEmailVerified(false);
        Users saved = repository.save(newUser);

        // Gửi email xác thực (không block luồng register nếu mail fail)
        try {
            emailVerificationService.sendVerificationEmail(saved);
        } catch (Exception e) {
            // log nhưng vẫn coi đăng ký thành công
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Đăng ký thành công. Kiểm tra email để xác thực tài khoản.");
    }

    @GetMapping("/auth/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok("Xác thực email thành công");
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        emailVerificationService.sendResetPasswordEmail(email);
        // Luôn trả 200 dù email có tồn tại hay không (chống enumeration)
        return ResponseEntity.ok("Nếu email tồn tại, link reset đã được gửi");
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestParam("newPassword") String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải tối thiểu 8 ký tự");
        }
        emailVerificationService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }
}
