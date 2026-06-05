package com.powerranger.mens_fashion_backend.dto.response;

public record UserResponse(
    Long id,
    String email,
    String phone,
    String fullName,
    boolean active,
    boolean admin
) {}
