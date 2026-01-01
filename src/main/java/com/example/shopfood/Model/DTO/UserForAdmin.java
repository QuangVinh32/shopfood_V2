package com.example.shopfood.Model.DTO;

import lombok.Data;

@Data
public class UserForAdmin {
    
    private Integer userId;

    private String username;

    private String password;

    private String email;

    private String fullName;

    private String role;

    private String image;

    private String phone;
}