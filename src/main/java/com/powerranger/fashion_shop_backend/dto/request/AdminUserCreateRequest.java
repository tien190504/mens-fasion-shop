package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminUserCreateRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @Size(max = 150, message = "Full name must be at most 150 characters")
    String fullName,

    @Size(max = 20, message = "Phone must be at most 20 characters")
    String phone,

    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    String avatarUrl,

    @PastOrPresent(message = "Date of birth cannot be in the future")
    LocalDate dateOfBirth,

    Boolean active,
    Boolean admin
) {}
