package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
    @NotBlank(message = "Brand name is required") String name,
    String logoUrl,
    String description
) {}
