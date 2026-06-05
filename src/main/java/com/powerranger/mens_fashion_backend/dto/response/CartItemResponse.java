package com.powerranger.mens_fashion_backend.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
    Long id,
    Long variantId,
    String productName,
    String variantLabel,
    String sku,
    BigDecimal price,
    int quantity,
    BigDecimal lineTotal
) {}
