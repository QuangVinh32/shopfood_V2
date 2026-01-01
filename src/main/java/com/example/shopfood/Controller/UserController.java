package com.example.shopfood.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.shopfood.Model.DTO.UserForAdmin;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.User.UserRequest;
import com.example.shopfood.Service.Class.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin({"*"})
@Validated
@RestController
@RequestMapping({"/api/v1/users"})
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper mapper;

    @GetMapping({"/get-all"})
    public ResponseEntity<List<UserForAdmin>> getAllUser() {
        List<Users> users = this.userService.getAllUser();
        List<UserForAdmin> userDTOs = users.stream().map((user) ->mapper.map(user, UserForAdmin.class)).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(userDTOs);
    }

    @GetMapping({"/{userId}"})
    public ResponseEntity<?> getUserDetails(@PathVariable Integer userId) {
        Optional<Users> optionalUsers = this.userService.getUserById(userId);
        if (optionalUsers.isPresent()) {
            Users users = (Users)optionalUsers.get();
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không có thông tin về id được tìm kiếm");
        }
    }

    @PostMapping({"/create"})
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequest) {
        userService.CreateUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping({"/edit/{userId}"})
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @RequestBody @Valid UserRequest userRequest) {
        Optional<Users> optionalUsers = userService.getUserById(userId);
        if (optionalUsers.isPresent()) {
            Users user = optionalUsers.get();
            if (user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể chỉnh sửa user có role là ADMIN");
            } else {
                userService.updateUser(userId, userRequest);
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("USER không có thông tin");
        }
    }

    @DeleteMapping({"/{userId}"})
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        Optional<Users> userToDelete = userService.getUserById(userId);
        if (userToDelete.isPresent()) {
            Users user = userToDelete.get();
            if (user.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không thể xóa user có role là ADMIN");
            } else {
                boolean delete = userService.deleteUser(userId);
                return delete ? ResponseEntity.ok("Xóa user thành công") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Xóa không thành công");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("USER không có thông tin");
        }
    }
}
