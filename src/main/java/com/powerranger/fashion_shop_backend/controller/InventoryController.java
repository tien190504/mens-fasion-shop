package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.response.MovementResponseDTO;
import com.powerranger.fashion_shop_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/variants/{variantId}/movements")
    public ResponseEntity<ApiResponse<List<MovementResponseDTO>>> getMovements(@PathVariable Long variantId) {
        List<MovementResponseDTO> response = inventoryService.getMovementsByVariant(variantId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<Void>> addMovement(
            @RequestParam Long variantId,
            @RequestParam int quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String note) {
        inventoryService.addMovement(variantId, quantity, reason, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Inventory movement logged", null));
    }
}
