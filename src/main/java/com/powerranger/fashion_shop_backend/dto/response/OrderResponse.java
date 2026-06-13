package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderCode,
    String recipientName,
    String recipientPhone,
    String shippingAddress,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    String status,
    String paymentStatus,
    String paymentMethod,
    String note,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<OrderItemResponse> items
) {}
