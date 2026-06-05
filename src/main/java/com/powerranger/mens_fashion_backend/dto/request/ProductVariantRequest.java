package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductVariantRequest(
    @NotBlank(message = "SKU is required") String sku,
    String size,
    String color,
    @NotNull(message = "Price is required") BigDecimal price,
    int stockQuantity,
    String imageUrl,
    Boolean active
) {}
