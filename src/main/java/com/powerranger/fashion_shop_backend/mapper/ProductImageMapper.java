package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.ProductImage;
import com.powerranger.fashion_shop_backend.dto.response.ProductImageResponse;

public final class ProductImageMapper {
    private ProductImageMapper() {}

    public static ProductImageResponse toResponse(ProductImage i) {
        if (i == null) return null;
        return new ProductImageResponse(
            i.getId(),
            i.getImageUrl(),
            i.getAltText(),
            i.isPrimary(),
            i.getSortOrder()
        );
    }
}
