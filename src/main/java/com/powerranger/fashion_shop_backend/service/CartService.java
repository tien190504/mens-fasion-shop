package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.CartItemRequest;
import com.powerranger.fashion_shop_backend.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email, String sessionToken);
    CartResponse addItem(String email, String sessionToken, CartItemRequest request);
    CartResponse updateItemQuantity(String email, String sessionToken, Long itemId, int quantity);
    CartResponse removeItem(String email, String sessionToken, Long itemId);
    void clearCart(String email, String sessionToken);
}
