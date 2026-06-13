package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.dto.response.ProductVariantResponse;

public final class ProductVariantMapper {
    private ProductVariantMapper() {}

    public static ProductVariantResponse toResponse(ProductVariant v) {
        if (v == null) return null;
        return new ProductVariantResponse(
            v.getId(),
            v.getSku(),
            v.getSize(),
            v.getColor(),
            v.getPrice(),
            v.getStockQuantity(),
            v.getImageUrl(),
            v.isActive()
        );
    }
}
