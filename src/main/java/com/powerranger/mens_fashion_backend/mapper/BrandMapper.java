package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Brand;
import com.powerranger.mens_fashion_backend.dto.response.BrandResponse;

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
