package com.powerranger.fashion_shop_backend.dto.response;

import java.time.OffsetDateTime;

public record ShipmentStatusHistoryResponse(
    Long id,
    Long shipmentId,
    String status,
    String description,
    OffsetDateTime createdAt
) {}
