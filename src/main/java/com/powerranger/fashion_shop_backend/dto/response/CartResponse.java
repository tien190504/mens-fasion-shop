package com.powerranger.fashion_shop_backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    Long id,
    String sessionToken,
    List<CartItemResponse> items,
    BigDecimal totalAmount
) {}
