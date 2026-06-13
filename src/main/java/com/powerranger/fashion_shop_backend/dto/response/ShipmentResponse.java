package com.powerranger.fashion_shop_backend.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ShipmentResponse(
    Long id,
    Long orderId,
    String trackingNumber,
    String carrier,
    String status,
    OffsetDateTime shippedAt,
    LocalDate estimatedDelivery
) {}
