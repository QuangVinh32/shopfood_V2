package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Role;
import lombok.Data;

@Data
public class LoginDTO {
    private int userId;
    private String fullName;
    private String username;
    private String image;
    private String phone;
    private String address;
    private String email;
    private Role role;
    private String userAgent;
    private String token;
}
