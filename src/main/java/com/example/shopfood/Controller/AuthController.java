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
import com.example.shopfood.Model.Entity.Role;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.User.LoginRequest;
import com.example.shopfood.Model.Request.User.UserRequest;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IFileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Autowired
    private UserRepository repository;
    @Autowired
    private IFileService fileService;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Value("${app.public.base-url:http://localhost:8080}")
    private String publicBaseUrl;

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
            // Đã hết khóa → reset
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
            // Generic error → chống user enumeration
            throw new AppException(ErrorResponseBase.INVALID_CREDENTIAL);
        }

        // ✅ Login thành công
        loginAttemptMap.remove(key);
        lockoutMap.remove(key);

        Users user = userOptional.get();
        LoginDTO loginDTO = new LoginDTO();
        BeanUtils.copyProperties(user, loginDTO);

        if (user.getImage() != null) {
            String fileName = Paths.get(user.getImage()).getFileName().toString();
            loginDTO.setImage(publicBaseUrl + "/files/image/" + fileName);
        }

        loginDTO.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        loginDTO.setToken(jwtTokenUtils.createAccessToken(loginDTO));

        return loginDTO;
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
        repository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công");
    }
}
