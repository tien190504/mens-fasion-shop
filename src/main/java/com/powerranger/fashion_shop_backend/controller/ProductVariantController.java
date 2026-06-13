package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.ProductVariantRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductVariantResponse;
import com.powerranger.fashion_shop_backend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/variants")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<ProductVariantResponse> response = productVariantService.getAll(productId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getById(@PathVariable Long id) {
        ProductVariantResponse response = productVariantService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVariantResponse>> create(
            @Valid @RequestBody ProductVariantRequest request) {
        ProductVariantResponse response = productVariantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Variant created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {
        ProductVariantResponse response = productVariantService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Variant updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productVariantService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok("Variant deleted", null));
    }
}
