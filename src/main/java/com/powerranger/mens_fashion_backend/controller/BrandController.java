package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.request.BrandRequest;
import com.powerranger.mens_fashion_backend.dto.response.BrandResponse;
import com.powerranger.mens_fashion_backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(brandService.listAll()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(brandService.getBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Brand created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Brand updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok("Brand deleted", null));
    }
}
