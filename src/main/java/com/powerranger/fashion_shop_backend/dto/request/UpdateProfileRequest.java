package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
    @Size(max = 150, message = "Full name must be at most 150 characters") String fullName,
    @Size(max = 20, message = "Phone must be at most 20 characters") String phone,
    @Size(max = 500, message = "Avatar URL must be at most 500 characters") String avatarUrl,
    @PastOrPresent(message = "Date of birth cannot be in the future") LocalDate dateOfBirth,
    String currentPassword,
    @Size(min = 6, message = "Password must be at least 6 characters") String newPassword
) {}
