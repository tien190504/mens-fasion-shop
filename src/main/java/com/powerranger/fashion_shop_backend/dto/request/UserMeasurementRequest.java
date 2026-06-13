package com.powerranger.fashion_shop_backend.dto.request;

import java.math.BigDecimal;

public record UserMeasurementRequest(
    BigDecimal heightCm,
    BigDecimal weightKg,
    BigDecimal chestCm,
    BigDecimal waistCm,
    BigDecimal hipCm,
    BigDecimal shoulderCm
) {}
