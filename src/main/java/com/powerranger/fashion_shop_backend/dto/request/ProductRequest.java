package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
    @NotBlank(message = "Product name is required") String name,
    String description,
    @NotNull(message = "Brand ID is required") Long brandId,
    @NotNull(message = "Category ID is required") Long categoryId,
    @NotNull(message = "Base price is required") BigDecimal basePrice,
    String gender,
    Boolean published,
    List<Long> secondaryCategoryIds,
    List<ProductVariantRequest> variants,
    List<String> imageUrls
) {}
