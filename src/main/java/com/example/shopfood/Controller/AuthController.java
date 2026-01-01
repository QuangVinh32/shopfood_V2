package com.example.shopfood.Controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api"})
@CrossOrigin({"*"})
@Validated
@Component
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
    private final Map<String, Integer> loginAttemptMap = new HashMap<>();
    private final Map<String, LocalDateTime> lockoutMap = new HashMap<>();

    @PostMapping("/login")
    public LoginDTO loginJWT(@RequestBody LoginRequest request) {

        String username = request.getUsername();
        // Khóa theo IP + username
        String ip = httpServletRequest.getRemoteAddr();
        String key = username + ":" + ip;

        LocalDateTime lockoutTime = lockoutMap.get(key);
        if (lockoutTime != null && lockoutTime.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorResponseBase.Login_locked);
        }

        int loginAttempts = loginAttemptMap.getOrDefault(key, 0);
        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockoutMap.put(key, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            throw new AppException(ErrorResponseBase.Login_locked);
        }

        Optional<Users> userOptional = repository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new AppException(ErrorResponseBase.Login_locked); // không lộ user
        }

        Users user = userOptional.get();
        if (!encoder.matches(request.getPassword(), user.getPassword())) {

            loginAttempts++;
            loginAttemptMap.put(key, loginAttempts);

            throw new AppException(ErrorResponseBase.Login_locked);
        }

        // ✅ Login thành công
        loginAttemptMap.remove(key);
        lockoutMap.remove(key);

        LoginDTO loginDTO = new LoginDTO();
        BeanUtils.copyProperties(user, loginDTO);

        if (user.getImage() != null) {
            String fileName = Paths.get(user.getImage()).getFileName().toString();
            loginDTO.setImage("http://localhost:8080/files/image/" + fileName);
        }

        loginDTO.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        loginDTO.setToken(jwtTokenUtils.createAccessToken(loginDTO));

        return loginDTO;
    }


    @PostMapping({"/register"})
    public ResponseEntity<?> registerUser(@ModelAttribute UserRequest userRequest) throws IOException {
        Optional<Users> existingUser = repository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new AppException(ErrorResponseBase.USERNAME_EXISTS);
        } else {
            Optional<Users> existingEmail = repository.findByEmail(userRequest.getEmail());
            if (existingEmail.isPresent()) {
                throw new AppException(ErrorResponseBase.DOUBLE_EMAIL_EX);
            } else {
                Users newUser = new Users();
                newUser.setAddress(userRequest.getAddress());
                newUser.setEmail(userRequest.getEmail());
                newUser.setPassword(encoder.encode(userRequest.getPassword()));
                newUser.setUsername(userRequest.getUsername());
                newUser.setPhone(userRequest.getPhone());
//                newUser.setImage(userRequest.getImage());
                String imagePath = fileService.uploadImage(userRequest.getImage());
                newUser.setImage(imagePath);

                newUser.setRole(Role.USER);
                newUser.setFullName(userRequest.getFullName());
                repository.save(newUser);
                return ResponseEntity.status(HttpStatus.CREATED).body("thêm mới thành công ");
            }
        }
    }
}

