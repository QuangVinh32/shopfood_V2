package com.example.shopfood.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.shopfood.Model.DTO.UserForAdmin;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.User.ChangePasswordRequest;
import com.example.shopfood.Model.Request.User.UserRequest;
import com.example.shopfood.Service.Class.UserService;
import com.example.shopfood.Utils.CurrentUserUtil;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping({"/api/v1/users"})
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CurrentUserUtil currentUserUtil;

    @GetMapping({"/get-all"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserForAdmin>> getAllUser() {
        List<Users> users = this.userService.getAllUser();
        List<UserForAdmin> userDTOs = users.stream()
                .map(user -> mapper.map(user, UserForAdmin.class))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(userDTOs);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(
                currentUserUtil.currentUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @GetMapping("/me")
    public ResponseEntity<UserForAdmin> getMyProfile() {
        Users me = currentUserUtil.currentUser();
        return ResponseEntity.ok(mapper.map(me, UserForAdmin.class));
    }

    @GetMapping({"/{userId}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getUserDetails(@PathVariable Integer userId) {
        Optional<Users> optionalUsers = userService.getUserById(userId);
        if (optionalUsers.isPresent()) {
            return ResponseEntity.ok(mapper.map(optionalUsers.get(), UserForAdmin.class));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Không có thông tin về id được tìm kiếm");
    }

    @PostMapping({"/create"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping({"/edit/{userId}"})
    public ResponseEntity<?> updateUser(@PathVariable Integer userId,
                                        @RequestBody @Valid UserRequest userRequest) {
        Optional<Users> optionalUsers = userService.getUserById(userId);
        if (optionalUsers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("USER không có thông tin");
        }
        Users target = optionalUsers.get();
        Users me = currentUserUtil.currentUser();
        boolean isAdmin = currentUserUtil.hasRole("ADMIN");

        if (!isAdmin && !me.getUserId().equals(target.getUserId())) {
            throw new AccessDeniedException("Bạn chỉ sửa được hồ sơ của chính mình");
        }
        if (!isAdmin && target.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể chỉnh sửa user có role là ADMIN");
        }
        userService.updateUser(userId, userRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping({"/{userId}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        Optional<Users> userToDelete = userService.getUserById(userId);
        if (userToDelete.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("USER không có thông tin");
        }
        Users user = userToDelete.get();
        if (user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa user có role là ADMIN");
        }
        boolean delete = userService.deleteUser(userId);
        return delete
                ? ResponseEntity.ok("Xóa user thành công")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Xóa không thành công");
    }
}
