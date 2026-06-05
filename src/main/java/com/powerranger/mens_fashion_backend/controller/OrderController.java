package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.request.OrderRequest;
import com.powerranger.mens_fashion_backend.dto.request.OrderStatusRequest;
import com.powerranger.mens_fashion_backend.dto.response.OrderResponse;
import com.powerranger.mens_fashion_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            JwtAuthenticationToken jwt,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(jwt.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order placed successfully", response));
    }

    @GetMapping("/api/v1/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            JwtAuthenticationToken jwt,
            @PathVariable Long id) {
        OrderResponse response = orderService.getOrder(id, jwt.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listMyOrders(
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        Page<OrderResponse> response = orderService.listMyOrders(jwt.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listAllOrders(Pageable pageable) {
        Page<OrderResponse> response = orderService.listAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/api/v1/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", response));
    }
}
