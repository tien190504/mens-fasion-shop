package com.powerranger.mens_fashion_backend.dto.response;

public record BrandResponse(
    Long id,
    String name,
    String slug,
    String logoUrl,
    String description
) {}
