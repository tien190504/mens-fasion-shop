package com.powerranger.mens_fashion_backend.dto.response;

public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String description,
    int sortOrder,
    boolean active,
    Long parentId,
    String parentName
) {}
