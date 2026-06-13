package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long variantId,
    String productName,
    String variantLabel,
    String sku,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal lineTotal
) {}
