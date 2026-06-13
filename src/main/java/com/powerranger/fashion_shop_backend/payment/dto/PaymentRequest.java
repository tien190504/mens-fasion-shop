package com.powerranger.fashion_shop_backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
    @NotNull(message = "Order ID is required") Long orderId,
    @NotBlank(message = "Payment method is required") String method
) {}
