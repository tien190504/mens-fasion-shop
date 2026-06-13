package com.powerranger.fashion_shop_backend.dto.response;

import java.time.OffsetDateTime;

public record ReviewResponse(
    Long id,
    String userName,
    int rating,
    String comment,
    OffsetDateTime createdAt
) {}
