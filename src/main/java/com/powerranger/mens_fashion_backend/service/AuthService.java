package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.LoginRequest;
import com.powerranger.mens_fashion_backend.dto.request.RegisterRequest;
import com.powerranger.mens_fashion_backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
