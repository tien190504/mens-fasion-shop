package com.powerranger.mens_fashion_backend.dto.response;

import java.math.BigDecimal;

public record UserMeasurementResponse(
    Long id,
    BigDecimal heightCm,
    BigDecimal weightKg,
    BigDecimal chestCm,
    BigDecimal waistCm,
    BigDecimal hipCm,
    BigDecimal shoulderCm
) {}
