package com.powerranger.mens_fashion_backend.dto.response;

import java.math.BigDecimal;

public record ProductSummaryResponse(
    Long id,
    String name,
    String slug,
    String description,
    BigDecimal basePrice,
    String primaryImageUrl
) {}
