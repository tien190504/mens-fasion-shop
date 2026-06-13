package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.ImageOrderRequest;
import com.powerranger.fashion_shop_backend.dto.request.ImageReorderRequest;
import com.powerranger.fashion_shop_backend.dto.request.ProductImageRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductImageResponse;
import com.powerranger.fashion_shop_backend.entity.Product;
import com.powerranger.fashion_shop_backend.entity.ProductImage;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.ProductImageMapper;
import com.powerranger.fashion_shop_backend.repository.ProductImageRepository;
import com.powerranger.fashion_shop_backend.repository.ProductRepository;
import com.powerranger.fashion_shop_backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        boolean isPrimary = request.primary() != null ? request.primary() : false;

        // If this is the first image, or marked primary, reset others
        if (isPrimary || product.getImages().isEmpty()) {
            isPrimary = true;
            product.getImages().forEach(img -> img.setPrimary(false));
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(request.imageUrl());
        image.setAltText(request.altText());
        image.setPrimary(isPrimary);
        image.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);

        product.getImages().add(image);
        productRepository.save(product);

        // Fetch saved image to get ID
        ProductImage saved = product.getImages().stream()
                .filter(i -> i.getImageUrl().equals(request.imageUrl()))
                .findFirst()
                .orElse(image);

        return ProductImageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product image not found"));

        Product product = image.getProduct();
        boolean wasPrimary = image.isPrimary();

        product.getImages().remove(image);
        productImageRepository.delete(image);

        // If the deleted image was primary, promote another one
        if (wasPrimary && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimary(true);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void reorderImages(ImageReorderRequest request) {
        for (ImageOrderRequest imgOrder : request.images()) {
            ProductImage image = productImageRepository.findById(imgOrder.id())
                    .orElseThrow(() -> new NotFoundException("Product image not found: " + imgOrder.id()));
            image.setSortOrder(imgOrder.sortOrder());
            productImageRepository.save(image);
        }
    }
}
