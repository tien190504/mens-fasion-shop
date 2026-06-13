package com.powerranger.fashion_shop_backend.dto.response;

import java.time.OffsetDateTime;

public record MovementResponseDTO(
    Long id,
    Long variantId,
    String sku,
    int changeQty,
    String reason,
    Long orderId,
    String note,
    OffsetDateTime createdAt
) {}
