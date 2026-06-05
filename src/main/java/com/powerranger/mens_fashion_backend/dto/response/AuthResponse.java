package com.powerranger.mens_fashion_backend.dto.response;

public record AuthResponse(
    String token,
    String email,
    String fullName,
    boolean isAdmin
) {}
