package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.CategoryRequest;
import com.powerranger.fashion_shop_backend.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> listAll();
    CategoryResponse getBySlug(String slug);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
