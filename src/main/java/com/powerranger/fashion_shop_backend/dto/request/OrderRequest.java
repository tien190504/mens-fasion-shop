package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OrderRequest(
    String recipientName,
    String recipientPhone,
    String shippingAddress,
    @NotBlank(message = "Payment method is required") String paymentMethod,
    String couponCode,
    String note,
    Long addressId
) {}
