package com.powerranger.fashion_shop_backend.payment.dto;

public record PaymentResponse(
    String paymentUrl,
    String transactionRef
) {}
