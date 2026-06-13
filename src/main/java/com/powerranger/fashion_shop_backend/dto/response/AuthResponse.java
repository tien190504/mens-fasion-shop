package com.powerranger.fashion_shop_backend.dto.response;

public record AuthResponse(
    String token,
    String email,
    String fullName,
    boolean isAdmin
) {}
