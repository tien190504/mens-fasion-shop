package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ImageOrderRequest(
    @NotNull(message = "Image ID is required") Long id,
    @NotNull(message = "Sort order is required") @PositiveOrZero(message = "Sort order must be >= 0") Integer sortOrder
) {}
