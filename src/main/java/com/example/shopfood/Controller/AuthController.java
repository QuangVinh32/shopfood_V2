package com.example.shopfood.Controller;

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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private BCryptPasswordEncoder encoder;
    @Autowired
    private HttpServletRequest httpServletRequest;
    private final Map<String, Integer> loginAttemptMap = new HashMap<>();
    private final Map<String, LocalDateTime> lockoutMap = new HashMap<>();

    @PostMapping({"/login"})
    public LoginDTO loginJWT(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        LocalDateTime lockoutTime = lockoutMap.get(username);
        if (lockoutTime != null && lockoutTime.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorResponseBase.Login_locked);
        } else {
            int loginAttempts = loginAttemptMap.getOrDefault(username, 0);
            if (loginAttempts >= 5) {
                LocalDateTime lockoutEndTime = LocalDateTime.now().plusMinutes(10L);
                lockoutMap.put(username, lockoutEndTime);
                throw new AppException(ErrorResponseBase.Login_locked);
            } else {
                Optional<Users> userOptional = repository.findByUsername(username);
                if (userOptional.isEmpty()) {
                    throw new AppException(ErrorResponseBase.Login_username);
                } else {
                    Users user = (Users)userOptional.get();
                    if (!encoder.matches(request.getPassword(), user.getPassword())) {
                        ++loginAttempts;
                        loginAttemptMap.put(username, loginAttempts);
                        if (loginAttempts == 5) {
                            throw new AppException(ErrorResponseBase.valueOf("số lần đăng nhập còn 1 lần nếu bạn nhập sai lần nữa sẽ bị khóa "));
                        } else if (loginAttempts >= 5) {
                            LocalDateTime lockoutEndTime = LocalDateTime.now().plusMinutes(10L);
                            lockoutMap.put(username, lockoutEndTime);
                            throw new AppException(ErrorResponseBase.Login_locked);
                        } else {
                            throw new AppException(ErrorResponseBase.Login_password);
                        }
                    } else {
                        loginAttemptMap.remove(username);
                        lockoutMap.remove(username);
                        LoginDTO loginDTO = new LoginDTO();
                        BeanUtils.copyProperties(user, loginDTO);
                        loginDTO.setUserAgent(httpServletRequest.getHeader("User-Agent"));
                        String token = jwtTokenUtils.createAccessToken(loginDTO);
                        loginDTO.setToken(token);
                        return loginDTO;
                    }
                }
            }
        }
    }

    @PostMapping({"/register"})
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest) {
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
                newUser.setImage(userRequest.getImage());
                newUser.setRole(Role.USER);
                newUser.setFullName(userRequest.getFullName());
                repository.save(newUser);
                return ResponseEntity.status(HttpStatus.CREATED).body("thêm mới thành công ");
            }
        }
    }
}

