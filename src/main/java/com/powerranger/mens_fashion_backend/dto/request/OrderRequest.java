package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OrderRequest(
    @NotBlank(message = "Recipient name is required") String recipientName,
    @NotBlank(message = "Recipient phone is required") String recipientPhone,
    @NotBlank(message = "Shipping address is required") String shippingAddress,
    @NotBlank(message = "Payment method is required") String paymentMethod,
    String couponCode,
    String note
) {}
