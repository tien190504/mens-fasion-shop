package com.powerranger.fashion_shop_backend.service;

public interface PasswordResetTokenService {
    String generateToken(String email);
    void resetPassword(String token, String newPassword);
}
