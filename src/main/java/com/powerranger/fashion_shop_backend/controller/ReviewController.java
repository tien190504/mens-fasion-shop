package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.ReviewRequest;
import com.powerranger.fashion_shop_backend.dto.response.ReviewResponse;
import com.powerranger.fashion_shop_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> listByProduct(
            @PathVariable Long productId,
            Pageable pageable) {
        Page<ReviewResponse> response = reviewService.listByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            JwtAuthenticationToken jwt,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.create(jwt.getName(), productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Review added", response));
    }

    @DeleteMapping("/api/v1/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(JwtAuthenticationToken jwt, @PathVariable Long id) {
        reviewService.delete(jwt.getName(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok("Review deleted", null));
    }
}
