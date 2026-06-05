package com.powerranger.mens_fashion_backend.dto.response;

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
