package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Cart;
import com.powerranger.mens_fashion_backend.entity.CartItem;
import com.powerranger.mens_fashion_backend.dto.response.CartResponse;
import com.powerranger.mens_fashion_backend.dto.response.CartItemResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class CartMapper {
    private CartMapper() {}

    public static CartItemResponse toItemResponse(CartItem item) {
        if (item == null) return null;
        BigDecimal price = item.getVariant() != null ? item.getVariant().getPrice() : BigDecimal.ZERO;
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
        
        String productName = "";
        String sku = "";
        String size = "";
        String color = "";
        if (item.getVariant() != null) {
            sku = item.getVariant().getSku();
            size = item.getVariant().getSize();
            color = item.getVariant().getColor();
            if (item.getVariant().getProduct() != null) {
                productName = item.getVariant().getProduct().getName();
            }
        }
        
        String variantLabel = (size != null ? size : "") + " / " + (color != null ? color : "");
        
        return new CartItemResponse(
            item.getId(),
            item.getVariant() != null ? item.getVariant().getId() : null,
            productName,
            variantLabel,
            sku,
            price,
            item.getQuantity(),
            lineTotal
        );
    }

    public static CartResponse toResponse(Cart cart) {
        if (cart == null) return null;
        
        List<CartItemResponse> itemResponses = cart.getItems() != null ?
            cart.getItems().stream().map(CartMapper::toItemResponse).toList() : Collections.emptyList();
            
        BigDecimal totalAmount = itemResponses.stream()
            .map(CartItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
            cart.getId(),
            itemResponses,
            totalAmount
        );
    }
}
