package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CouponRequest(
    @NotBlank(message = "Coupon code is required") String code,
    String description,
    @NotBlank(message = "Discount type is required") String discountType,
    @NotNull(message = "Discount value is required") BigDecimal discountValue,
    BigDecimal minOrderAmount,
    BigDecimal maxDiscountAmount,
    Integer usageLimit,
    Integer usageLimitPerUser,
    OffsetDateTime validFrom,
    OffsetDateTime validUntil,
    Boolean active,
    List<Long> productIds,
    List<Long> categoryIds
) {}
