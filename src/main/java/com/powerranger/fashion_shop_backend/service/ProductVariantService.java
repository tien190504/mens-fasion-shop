package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.ProductVariantRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductVariantResponse;
import java.util.List;

public interface ProductVariantService {
    List<ProductVariantResponse> getAll(Long productId);
    ProductVariantResponse getById(Long id);
    ProductVariantResponse create(ProductVariantRequest request);
    ProductVariantResponse update(Long id, ProductVariantRequest request);
    void delete(Long id);
}
