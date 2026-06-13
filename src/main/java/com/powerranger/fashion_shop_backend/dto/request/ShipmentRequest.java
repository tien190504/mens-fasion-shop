package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ShipmentRequest(
    @NotNull(message = "Order ID is required") Long orderId,
    @NotBlank(message = "Carrier is required") String carrier,
    @NotBlank(message = "Tracking number is required") String trackingNumber,
    BigDecimal shippingFee
) {}
