package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.BrandRequest;
import com.powerranger.mens_fashion_backend.dto.response.BrandResponse;
import java.util.List;

public interface BrandService {
    List<BrandResponse> listAll();
    BrandResponse getBySlug(String slug);
    BrandResponse create(BrandRequest request);
    BrandResponse update(Long id, BrandRequest request);
    void delete(Long id);
}
