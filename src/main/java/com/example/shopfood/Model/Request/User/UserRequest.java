package com.example.shopfood.Model.Request.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    private @NotBlank(
            message = "Username is required"
    ) String username;
    private @NotBlank(
            message = "Password is required"
    ) String password;
    private @NotBlank(
            message = "Email is required"
    ) @Email(
            message = "Invalid email format"
    ) String email;
    private @NotBlank(
            message = "Full name is required"
    ) String fullName;
    private @NotBlank(
            message = "Address is required"
    ) String address;
    private @NotBlank(
            message = "image is required"
    ) String image;
    private @NotBlank(
            message = "Phone number is required"
    ) String phone;
}
