package com.powerranger.mens_fashion_backend.dto.response;

public record ProductImageResponse(
    Long id,
    String imageUrl,
    String altText,
    boolean primary,
    int sortOrder
) {}
