package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.RefreshToken;
import com.example.shopfood.Model.Entity.Users;
import jakarta.servlet.http.HttpServletRequest;

public interface IRefreshTokenService {

    RefreshToken issue(Users user, HttpServletRequest request);

    RefreshToken rotate(String oldToken, HttpServletRequest request);

    void revoke(String token);

    void revokeAllForUser(Users user);

    Users validateAndGetUser(String token);
}
