package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.request.CouponRequest;
import com.powerranger.mens_fashion_backend.dto.response.CouponResponse;
import com.powerranger.mens_fashion_backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/api/v1/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(couponService.listAll()));
    }

    @GetMapping("/api/v1/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getById(id)));
    }

    @PostMapping("/api/v1/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> create(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Coupon created", response));
    }

    @PutMapping("/api/v1/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Coupon updated", response));
    }

    @GetMapping("/api/v1/coupons/validate")
    public ResponseEntity<ApiResponse<CouponResponse>> validate(
            JwtAuthenticationToken jwt,
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        String email = jwt != null ? jwt.getName() : null;
        CouponResponse response = couponService.validateCoupon(code, email, orderAmount);
        return ResponseEntity.ok(ApiResponse.ok("Coupon is valid", response));
    }

    @PatchMapping("/api/v1/coupons/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        couponService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.ok("Coupon active status toggled", null));
    }
}
