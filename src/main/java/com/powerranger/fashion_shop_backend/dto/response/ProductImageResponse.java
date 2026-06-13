package com.powerranger.fashion_shop_backend.dto.response;

public record ProductImageResponse(
    Long id,
    String imageUrl,
    String altText,
    boolean primary,
    int sortOrder
) {}
