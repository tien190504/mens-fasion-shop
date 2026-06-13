package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.LoginRequest;
import com.powerranger.fashion_shop_backend.dto.request.RegisterRequest;
import com.powerranger.fashion_shop_backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
