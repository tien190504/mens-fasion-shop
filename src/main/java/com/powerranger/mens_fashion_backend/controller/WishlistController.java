package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import com.powerranger.mens_fashion_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> list(JwtAuthenticationToken jwt) {
        List<ProductSummaryResponse> response = wishlistService.list(jwt.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> add(JwtAuthenticationToken jwt, @PathVariable Long productId) {
        wishlistService.add(jwt.getName(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Product added to wishlist", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(JwtAuthenticationToken jwt, @PathVariable Long productId) {
        wishlistService.remove(jwt.getName(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Product removed from wishlist", null));
    }
}
