package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InvoiceResponse(
    String invoiceNumber,
    String orderCode,
    String recipientName,
    String recipientPhone,
    String shippingAddress,
    OffsetDateTime orderDate,
    List<InvoiceItemResponse> items,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    String paymentMethod,
    String paymentStatus
) {}
