package com.example.shopfood.Service.Class;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.example.shopfood.Exception.AppException;
import com.example.shopfood.Exception.ErrorResponseBase;
import com.example.shopfood.Model.Entity.Role;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.User.UserRequest;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IUserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Validated
@Service
public class UserService implements IUserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> optional = userRepository.findByUsername(username);
        if (optional.isPresent()) {
            Users user = optional.get();
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
            return new User(user.getUsername(), user.getPassword(), authorities);
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    @Override
    public List<Users> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public List<Users> searchUsersByUsername(String username) {
        Optional<Users> optionalUsers = userRepository.findByUsername(username);
        return optionalUsers.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    @Override
    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<Users> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public void createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorResponseBase.DOUBLE_EMAIL_EX);
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorResponseBase.DOUBLE_USERNAME_EX);
        }
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new AppException(ErrorResponseBase.DUPLICATE_PHONE);
        }

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(encoder.encode(userRequest.getPassword()));
        users.setUsername(userRequest.getUsername());
        users.setAddress(userRequest.getAddress());
        users.setPhone(userRequest.getPhone());
        users.setRole(Role.MANAGER); // endpoint này chỉ admin gọi → MANAGER hợp lý
        users.setFullName(userRequest.getFullName());
        userRepository.save(users);
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public void updateUser(Integer userId, UserRequest userRequest) {
        Users users = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorResponseBase.Id_not));

        // Chỉ update field nếu được truyền vào (không null/blank)
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            users.setPassword(encoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getPhone() != null && !userRequest.getPhone().isBlank()) {
            users.setPhone(userRequest.getPhone());
        }
        if (userRequest.getFullName() != null && !userRequest.getFullName().isBlank()) {
            users.setFullName(userRequest.getFullName());
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().isBlank()) {
            users.setEmail(userRequest.getEmail());
        }
        if (userRequest.getAddress() != null && !userRequest.getAddress().isBlank()) {
            users.setAddress(userRequest.getAddress());
        }
        // username không cho đổi (immutable theo entity)
        userRepository.save(users);
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public boolean deleteUser(Integer userId) {
        Optional<Users> optionalUsers = userRepository.findById(userId);
        if (optionalUsers.isEmpty()) return false;
        Users u = optionalUsers.get();
        if (u.getOrders() != null && !u.getOrders().isEmpty()) {
            throw new RuntimeException("Không xóa được: user có lịch sử đơn hàng. Hãy vô hiệu hóa thay vì xóa.");
        }
        userRepository.delete(u);
        return true;
    }

    @Override
    @Transactional(rollbackOn = {Exception.class})
    public void changePassword(String username, String oldPassword, String newPassword) {

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorResponseBase.USER_NOT_FOUND));

        // Sai mật khẩu cũ
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorResponseBase.OLD_PASSWORD_INCORRECT);
        }

        // Encode mật khẩu mới
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

}
