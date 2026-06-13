package com.powerranger.fashion_shop_backend.dto.response;

import java.time.LocalDate;

public record UserResponse(
    Long id,
    String email,
    String phone,
    String fullName,
    String avatarUrl,
    LocalDate dateOfBirth,
    boolean active,
    boolean admin
) {}
