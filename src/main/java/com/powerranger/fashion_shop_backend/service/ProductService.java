package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.ProductRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductResponse;
import com.powerranger.fashion_shop_backend.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<ProductSummaryResponse> filter(
            String keyword,
            Long categoryId,
            Long brandId,
            String gender,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            Pageable pageable
    );

    ProductResponse getBySlug(String slug);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);

    List<String> autocomplete(String keyword);
}
