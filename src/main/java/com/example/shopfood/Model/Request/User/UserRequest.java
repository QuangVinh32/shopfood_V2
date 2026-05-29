package com.example.shopfood.Model.Request.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "Username chỉ chứa chữ, số, dấu chấm hoặc gạch dưới")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password phải từ 8 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String address;

    private MultipartFile image;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;
}
