package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CouponResponse(
    Long id,
    String code,
    String description,
    String discountType,
    BigDecimal discountValue,
    BigDecimal minOrderAmount,
    BigDecimal maxDiscountAmount,
    Integer usageLimit,
    Integer usageLimitPerUser,
    int usedCount,
    OffsetDateTime validFrom,
    OffsetDateTime validUntil,
    boolean active
) {}
