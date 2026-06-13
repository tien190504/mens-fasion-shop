package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.Coupon;
import com.powerranger.fashion_shop_backend.dto.response.CouponResponse;

public final class CouponMapper {
    private CouponMapper() {}

    public static CouponResponse toResponse(Coupon c) {
        if (c == null) return null;
        return new CouponResponse(
            c.getId(),
            c.getCode(),
            c.getDescription(),
            c.getDiscountType() != null ? c.getDiscountType().name() : null,
            c.getDiscountValue(),
            c.getMinOrderAmount(),
            c.getMaxDiscountAmount(),
            c.getUsageLimit(),
            c.getUsageLimitPerUser(),
            c.getUsedCount(),
            c.getValidFrom(),
            c.getValidUntil(),
            c.isActive()
        );
    }
}
