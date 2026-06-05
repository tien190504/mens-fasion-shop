package com.powerranger.mens_fashion_backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
    Long id,
    BrandResponse brand,
    CategoryResponse category,
    String name,
    String slug,
    String description,
    String gender,
    BigDecimal basePrice,
    boolean published,
    BigDecimal ratingAvg,
    int ratingCount,
    List<CategoryResponse> secondaryCategories,
    List<ProductImageResponse> images,
    List<ProductVariantResponse> variants
) {}
