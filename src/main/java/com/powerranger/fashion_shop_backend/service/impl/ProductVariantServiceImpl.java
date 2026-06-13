package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.ProductVariantRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductVariantResponse;
import com.powerranger.fashion_shop_backend.entity.Product;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.ProductVariantMapper;
import com.powerranger.fashion_shop_backend.repository.ProductRepository;
import com.powerranger.fashion_shop_backend.repository.ProductVariantRepository;
import com.powerranger.fashion_shop_backend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getAll(Long productId) {
        if (productId != null) {
            return productVariantRepository.findByProductIdOrderByIdAsc(productId).stream()
                    .map(ProductVariantMapper::toResponse)
                    .toList();
        }
        return productVariantRepository.findAll().stream()
                .map(ProductVariantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getById(Long id) {
        return productVariantRepository.findById(id)
                .map(ProductVariantMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Product variant not found"));
    }

    @Override
    @Transactional
    public ProductVariantResponse create(ProductVariantRequest request) {
        if (request.productId() == null) {
            throw new BadRequestException("Product ID is required");
        }
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (productVariantRepository.findBySku(request.sku()).isPresent()) {
            throw new BadRequestException("SKU already exists");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.sku());
        variant.setSize(request.size());
        variant.setColor(request.color());
        variant.setPrice(request.price());
        variant.setStockQuantity(request.stockQuantity());
        variant.setImageUrl(request.imageUrl());
        variant.setActive(request.active() != null ? request.active() : true);

        return ProductVariantMapper.toResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse update(Long id, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product variant not found"));

        if (!variant.getSku().equalsIgnoreCase(request.sku()) && 
            productVariantRepository.findBySku(request.sku()).isPresent()) {
            throw new BadRequestException("SKU already exists");
        }

        variant.setSku(request.sku());
        variant.setSize(request.size());
        variant.setColor(request.color());
        variant.setPrice(request.price());
        variant.setStockQuantity(request.stockQuantity());
        variant.setImageUrl(request.imageUrl());
        if (request.active() != null) {
            variant.setActive(request.active());
        }

        return ProductVariantMapper.toResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product variant not found"));
        productVariantRepository.delete(variant);
    }
}
