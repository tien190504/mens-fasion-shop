package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import java.util.List;

public interface WishlistService {
    List<ProductSummaryResponse> list(String email);
    void add(String email, Long productId);
    void remove(String email, Long productId);
}
