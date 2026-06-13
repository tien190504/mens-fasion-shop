package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.response.ShipmentStatusHistoryResponse;
import com.powerranger.fashion_shop_backend.service.ShipmentStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shipments/{shipmentId}/history")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ShipmentStatusHistoryController {

    private final ShipmentStatusHistoryService shipmentStatusHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentStatusHistoryResponse>>> getHistory(
            @PathVariable Long shipmentId) {
        List<ShipmentStatusHistoryResponse> response = shipmentStatusHistoryService.getHistory(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
