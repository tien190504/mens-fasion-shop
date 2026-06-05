package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.ProductRequest;
import com.powerranger.mens_fashion_backend.dto.response.ProductResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductSummaryResponse> filter(String keyword, Long categoryId, Long brandId, String gender, Pageable pageable);
    ProductResponse getBySlug(String slug);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
