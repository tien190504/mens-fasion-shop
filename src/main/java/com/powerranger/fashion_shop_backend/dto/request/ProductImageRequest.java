package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductImageRequest(
    @NotBlank(message = "Image URL is required") String imageUrl,
    String altText,
    Boolean primary,
    Integer sortOrder
) {}
