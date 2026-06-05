package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.ProductRequest;
import com.powerranger.mens_fashion_backend.dto.response.ProductResponse;
import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import com.powerranger.mens_fashion_backend.entity.Brand;
import com.powerranger.mens_fashion_backend.entity.Category;
import com.powerranger.mens_fashion_backend.entity.Product;
import com.powerranger.mens_fashion_backend.entity.ProductImage;
import com.powerranger.mens_fashion_backend.entity.ProductVariant;
import com.powerranger.mens_fashion_backend.entity.enums.GenderTarget;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.ProductMapper;
import com.powerranger.mens_fashion_backend.repository.BrandRepository;
import com.powerranger.mens_fashion_backend.repository.CategoryRepository;
import com.powerranger.mens_fashion_backend.repository.ProductRepository;
import com.powerranger.mens_fashion_backend.service.ProductService;
import com.powerranger.mens_fashion_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> filter(String keyword, Long categoryId, Long brandId, String gender, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, Category> secJoin = root.join("secondaryCategories", JoinType.LEFT);
                return cb.or(
                    cb.equal(root.get("category").get("id"), categoryId),
                    cb.equal(secJoin.get("id"), categoryId)
                );
            });
        }

        if (brandId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId));
        }

        if (gender != null && !gender.isBlank()) {
            try {
                GenderTarget genderEnum = GenderTarget.valueOf(gender.toLowerCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("gender"), genderEnum));
            } catch (IllegalArgumentException e) {
                // Invalid gender, skip or handle
            }
        }

        return productRepository.findAll(spec, pageable).map(ProductMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String slug = SlugUtil.from(request.name());
        if (productRepository.existsBySlug(slug)) {
            throw new BadRequestException("Product with similar name already exists");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(slug);
        product.setDescription(request.description());
        product.setBasePrice(request.basePrice());
        product.setPublished(request.published() != null ? request.published() : true);
        
        if (request.gender() != null) {
            product.setGender(GenderTarget.valueOf(request.gender().toLowerCase()));
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new NotFoundException("Brand not found"));
        product.setBrand(brand);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        product.setCategory(category);

        if (request.secondaryCategoryIds() != null) {
            List<Category> secondary = categoryRepository.findAllById(request.secondaryCategoryIds());
            product.setSecondaryCategories(new LinkedHashSet<>(secondary));
        }

        if (request.variants() != null) {
            List<ProductVariant> variants = new ArrayList<>();
            for (var vr : request.variants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSku(vr.sku());
                variant.setSize(vr.size());
                variant.setColor(vr.color());
                variant.setPrice(vr.price());
                variant.setStockQuantity(vr.stockQuantity());
                variant.setImageUrl(vr.imageUrl());
                variant.setActive(vr.active() != null ? vr.active() : true);
                variants.add(variant);
            }
            product.setVariants(variants);
        }

        if (request.imageUrls() != null) {
            List<ProductImage> images = new ArrayList<>();
            int sort = 0;
            for (String url : request.imageUrls()) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(url);
                img.setPrimary(sort == 0);
                img.setSortOrder(sort++);
                images.add(img);
            }
            product.setImages(images);
        }

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        String slug = SlugUtil.from(request.name());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new BadRequestException("Product with similar name already exists");
        }

        product.setName(request.name());
        product.setSlug(slug);
        product.setDescription(request.description());
        product.setBasePrice(request.basePrice());
        product.setPublished(request.published() != null ? request.published() : true);
        
        if (request.gender() != null) {
            product.setGender(GenderTarget.valueOf(request.gender().toLowerCase()));
        }

        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new NotFoundException("Brand not found"));
        product.setBrand(brand);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        product.setCategory(category);

        if (request.secondaryCategoryIds() != null) {
            List<Category> secondary = categoryRepository.findAllById(request.secondaryCategoryIds());
            product.getSecondaryCategories().clear();
            product.getSecondaryCategories().addAll(secondary);
        }

        // Simplistic replacement logic for images and variants
        if (request.variants() != null) {
            product.getVariants().clear();
            for (var vr : request.variants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSku(vr.sku());
                variant.setSize(vr.size());
                variant.setColor(vr.color());
                variant.setPrice(vr.price());
                variant.setStockQuantity(vr.stockQuantity());
                variant.setImageUrl(vr.imageUrl());
                variant.setActive(vr.active() != null ? vr.active() : true);
                product.getVariants().add(variant);
            }
        }

        if (request.imageUrls() != null) {
            product.getImages().clear();
            int sort = 0;
            for (String url : request.imageUrls()) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(url);
                img.setPrimary(sort == 0);
                img.setSortOrder(sort++);
                product.getImages().add(img);
            }
        }

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
