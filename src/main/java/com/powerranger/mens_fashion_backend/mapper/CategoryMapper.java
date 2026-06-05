package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Category;
import com.powerranger.mens_fashion_backend.dto.response.CategoryResponse;

public final class CategoryMapper {
    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category c) {
        if (c == null) return null;
        return new CategoryResponse(
            c.getId(),
            c.getName(),
            c.getSlug(),
            c.getDescription(),
            c.getSortOrder(),
            c.isActive(),
            c.getParent() != null ? c.getParent().getId() : null,
            c.getParent() != null ? c.getParent().getName() : null
        );
    }
}
