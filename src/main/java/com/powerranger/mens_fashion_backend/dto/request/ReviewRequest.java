package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(
    @Min(value = 1, message = "Rating must be at least 1") @Max(value = 5, message = "Rating cannot exceed 5") int rating,
    @NotBlank(message = "Comment is required") String comment
) {}
