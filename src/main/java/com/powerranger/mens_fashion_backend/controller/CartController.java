package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.request.CartItemRequest;
import com.powerranger.mens_fashion_backend.dto.response.CartResponse;
import com.powerranger.mens_fashion_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            JwtAuthenticationToken jwt,
            @RequestParam(required = false) String sessionToken) {
        String email = jwt != null ? jwt.getName() : null;
        CartResponse response = cartService.getCart(email, sessionToken);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            JwtAuthenticationToken jwt,
            @RequestParam(required = false) String sessionToken,
            @Valid @RequestBody CartItemRequest request) {
        String email = jwt != null ? jwt.getName() : null;
        CartResponse response = cartService.addItem(email, sessionToken, request);
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", response));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            JwtAuthenticationToken jwt,
            @RequestParam(required = false) String sessionToken,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        String email = jwt != null ? jwt.getName() : null;
        CartResponse response = cartService.updateItemQuantity(email, sessionToken, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.ok("Cart updated", response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            JwtAuthenticationToken jwt,
            @RequestParam(required = false) String sessionToken,
            @PathVariable Long itemId) {
        String email = jwt != null ? jwt.getName() : null;
        CartResponse response = cartService.removeItem(email, sessionToken, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            JwtAuthenticationToken jwt,
            @RequestParam(required = false) String sessionToken) {
        String email = jwt != null ? jwt.getName() : null;
        cartService.clearCart(email, sessionToken);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", null));
    }
}
