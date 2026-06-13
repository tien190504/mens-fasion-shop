package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.Brand;
import com.powerranger.fashion_shop_backend.dto.response.BrandResponse;

public final class BrandMapper {
    private BrandMapper() {}

    public static BrandResponse toResponse(Brand b) {
        if (b == null) return null;
        return new BrandResponse(
            b.getId(),
            b.getName(),
            b.getSlug(),
            b.getLogoUrl(),
            b.getDescription()
        );
    }
}
