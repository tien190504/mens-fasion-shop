package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Product;
import com.powerranger.mens_fashion_backend.entity.ProductImage;
import com.powerranger.mens_fashion_backend.entity.ProductVariant;
import com.powerranger.mens_fashion_backend.dto.response.ProductResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductImageResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductVariantResponse;

import java.util.Collections;
import java.util.List;

public final class ProductMapper {
    private ProductMapper() {}

    public static ProductVariantResponse toVariantResponse(ProductVariant v) {
        if (v == null) return null;
        return new ProductVariantResponse(
            v.getId(),
            v.getSku(),
            v.getSize(),
            v.getColor(),
            v.getPrice(),
            v.getStockQuantity(),
            v.getImageUrl(),
            v.isActive()
        );
    }

    public static ProductImageResponse toImageResponse(ProductImage i) {
        if (i == null) return null;
        return new ProductImageResponse(
            i.getId(),
            i.getImageUrl(),
            i.getAltText(),
            i.isPrimary(),
            i.getSortOrder()
        );
    }

    public static ProductResponse toResponse(Product p) {
        if (p == null) return null;
        
        List<ProductImageResponse> imgResponses = p.getImages() != null ?
            p.getImages().stream().map(ProductMapper::toImageResponse).toList() : Collections.emptyList();
            
        List<ProductVariantResponse> varResponses = p.getVariants() != null ?
            p.getVariants().stream().map(ProductMapper::toVariantResponse).toList() : Collections.emptyList();
            
        List<com.powerranger.mens_fashion_backend.dto.response.CategoryResponse> secCatResponses = p.getSecondaryCategories() != null ?
            p.getSecondaryCategories().stream().map(CategoryMapper::toResponse).toList() : Collections.emptyList();

        return new ProductResponse(
            p.getId(),
            BrandMapper.toResponse(p.getBrand()),
            CategoryMapper.toResponse(p.getCategory()),
            p.getName(),
            p.getSlug(),
            p.getDescription(),
            p.getGender() != null ? p.getGender().name() : null,
            p.getBasePrice(),
            p.isPublished(),
            p.getRatingAvg(),
            p.getRatingCount(),
            secCatResponses,
            imgResponses,
            varResponses
        );
    }

    public static ProductSummaryResponse toSummaryResponse(Product p) {
        if (p == null) return null;
        
        String primaryImg = null;
        if (p.getImages() != null) {
            primaryImg = p.getImages().stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(p.getImages().isEmpty() ? null : p.getImages().get(0).getImageUrl());
        }

        return new ProductSummaryResponse(
            p.getId(),
            p.getName(),
            p.getSlug(),
            p.getDescription(),
            p.getBasePrice(),
            primaryImg
        );
    }
}
