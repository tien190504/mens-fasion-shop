package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.response.ProductSummaryResponse;
import java.util.List;

public interface WishlistService {
    List<ProductSummaryResponse> list(String email);
    void add(String email, Long productId);
    void remove(String email, Long productId);
}
