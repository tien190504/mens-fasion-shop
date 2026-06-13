package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.BrandRequest;
import com.powerranger.fashion_shop_backend.dto.response.BrandResponse;
import java.util.List;

public interface BrandService {
    List<BrandResponse> listAll();
    BrandResponse getBySlug(String slug);
    BrandResponse create(BrandRequest request);
    BrandResponse update(Long id, BrandRequest request);
    void delete(Long id);
}
