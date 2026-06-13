package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;

public record ProductVariantResponse(
    Long id,
    String sku,
    String size,
    String color,
    BigDecimal price,
    int stockQuantity,
    String imageUrl,
    boolean active
) {}
