package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.ImageReorderRequest;
import com.powerranger.fashion_shop_backend.dto.request.ProductImageRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {
    ProductImageResponse addImage(Long productId, ProductImageRequest request);
    void deleteImage(Long id);
    void reorderImages(ImageReorderRequest request);
}
