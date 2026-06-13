package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.OrderItem;
import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import com.powerranger.fashion_shop_backend.dto.response.InvoiceItemResponse;
import com.powerranger.fashion_shop_backend.dto.response.InvoiceResponse;

import java.util.List;

public final class InvoiceMapper {
    private InvoiceMapper() {}

    public static InvoiceItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;
        return new InvoiceItemResponse(
            item.getProductName(),
            item.getVariantLabel(),
            item.getSku(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineTotal()
        );
    }

    public static InvoiceResponse toResponse(ShopOrder order) {
        if (order == null) return null;
        List<InvoiceItemResponse> items = order.getItems() != null ?
                order.getItems().stream().map(InvoiceMapper::toItemResponse).toList() : List.of();
        
        String invCode = order.getOrderCode().startsWith("ORD-") ? "INV-" + order.getOrderCode().substring(4)
                : "INV-" + order.getOrderCode();

        return new InvoiceResponse(
            invCode,
            order.getOrderCode(),
            order.getRecipientName(),
            order.getRecipientPhone(),
            order.getShippingAddress(),
            order.getCreatedAt(),
            items,
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getShippingFee(),
            order.getTotalAmount(),
            order.getPaymentMethod().name().toUpperCase(),
            order.getPaymentStatus().name().toUpperCase()
        );
    }
}
