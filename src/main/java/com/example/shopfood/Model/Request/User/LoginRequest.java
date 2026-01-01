package com.example.shopfood.Model.Request.User;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
