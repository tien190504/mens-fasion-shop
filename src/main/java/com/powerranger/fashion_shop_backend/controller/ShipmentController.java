package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.ShipmentRequest;
import com.powerranger.fashion_shop_backend.dto.response.ShipmentResponse;
import com.powerranger.fashion_shop_backend.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/admin/shipments/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getByOrder(@PathVariable Long orderId) {
        ShipmentResponse response = shipmentService.getByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/admin/shipments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(@Valid @RequestBody ShipmentRequest request) {
        ShipmentResponse response = shipmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Shipment logged", response));
    }

    @PatchMapping("/admin/shipments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ShipmentResponse response = shipmentService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Shipment status updated", response));
    }

    @GetMapping("/shipments/{trackingCode}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getByTrackingNumber(@PathVariable String trackingCode) {
        ShipmentResponse response = shipmentService.getByTrackingNumber(trackingCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
