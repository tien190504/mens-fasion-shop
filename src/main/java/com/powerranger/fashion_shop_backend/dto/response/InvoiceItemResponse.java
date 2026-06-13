package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;

public record InvoiceItemResponse(
    String productName,
    String variantLabel,
    String sku,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal lineTotal
) {}
