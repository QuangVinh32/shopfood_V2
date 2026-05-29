package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Users;

public interface IEmailVerificationService {

    void sendVerificationEmail(Users user);

    void verifyEmail(String token);

    void sendResetPasswordEmail(String email);

    void resetPassword(String token, String newPassword);
}
