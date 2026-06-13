package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequest(
    @NotBlank(message = "Status is required") String status,
    String note
) {}
