package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank(message = "Category name is required") String name,
    String description,
    Long parentId,
    int sortOrder,
    Boolean active
) {}
