package com.powerranger.fashion_shop_backend.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.dto.request.ImageReorderRequest;
import com.powerranger.fashion_shop_backend.dto.request.ProductImageRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductImageResponse;
import com.powerranger.fashion_shop_backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/products/{id}/images")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable Long id,
            @Valid @RequestBody ProductImageRequest request) {
        ProductImageResponse response = productImageService.addImage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Image added", response));
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long id) {
        productImageService.deleteImage(id);
        return ResponseEntity.ok(ApiResponse.ok("Image deleted", null));
    }

    @PutMapping("/images/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderImages(
            @Valid @RequestBody ImageReorderRequest request) {
        productImageService.reorderImages(request);
        return ResponseEntity.ok(ApiResponse.ok("Images reordered", null));
    }
}
