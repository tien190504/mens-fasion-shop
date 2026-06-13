package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import com.powerranger.fashion_shop_backend.entity.OrderItem;
import com.powerranger.fashion_shop_backend.dto.response.OrderResponse;
import com.powerranger.fashion_shop_backend.dto.response.OrderItemResponse;

import java.util.Collections;
import java.util.List;

public final class OrderMapper {
    private OrderMapper() {}

    public static OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponse(
            item.getId(),
            item.getVariant() != null ? item.getVariant().getId() : null,
            item.getProductName(),
            item.getVariantLabel(),
            item.getSku(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineTotal()
        );
    }

    public static OrderResponse toResponse(ShopOrder order) {
        if (order == null) return null;
        
        List<OrderItemResponse> itemResponses = order.getItems() != null ?
            order.getItems().stream().map(OrderMapper::toItemResponse).toList() : Collections.emptyList();

        return new OrderResponse(
            order.getId(),
            order.getOrderCode(),
            order.getRecipientName(),
            order.getRecipientPhone(),
            order.getShippingAddress(),
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getShippingFee(),
            order.getTotalAmount(),
            order.getStatus() != null ? order.getStatus().name() : null,
            order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null,
            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
            order.getNote(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            itemResponses
        );
    }
}
