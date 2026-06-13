package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.ProductRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductResponse;
import com.powerranger.fashion_shop_backend.dto.response.ProductSummaryResponse;
import com.powerranger.fashion_shop_backend.entity.Brand;
import com.powerranger.fashion_shop_backend.entity.Category;
import com.powerranger.fashion_shop_backend.entity.Product;
import com.powerranger.fashion_shop_backend.entity.ProductImage;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.entity.enums.GenderTarget;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.ProductMapper;
import com.powerranger.fashion_shop_backend.repository.BrandRepository;
import com.powerranger.fashion_shop_backend.repository.CategoryRepository;
import com.powerranger.fashion_shop_backend.repository.ProductRepository;
import com.powerranger.fashion_shop_backend.service.ProductService;
import com.powerranger.fashion_shop_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

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
    public Page<ProductSummaryResponse> filter(
            String keyword,
            Long categoryId,
            Long brandId,
            String gender,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> 
                cb.isTrue(cb.function("fts_match", Boolean.class, root.get("tsv"), cb.literal(keyword)))
            );
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

        if (size != null && !size.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                return cb.equal(cb.lower(variantJoin.get("size")), size.toLowerCase());
            });
        }

        if (color != null && !color.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                return cb.equal(cb.lower(variantJoin.get("color")), color.toLowerCase());
            });
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
        }

        Pageable sortedPageable = pageable;
        if (sort != null && !sort.isBlank()) {
            Sort sortObj = Sort.unsorted();
            if (sort.equalsIgnoreCase("price_asc")) {
                sortObj = Sort.by(Sort.Direction.ASC, "basePrice");
            } else if (sort.equalsIgnoreCase("price_desc")) {
                sortObj = Sort.by(Sort.Direction.DESC, "basePrice");
            } else if (sort.equalsIgnoreCase("date_asc")) {
                sortObj = Sort.by(Sort.Direction.ASC, "createdAt");
            } else if (sort.equalsIgnoreCase("date_desc")) {
                sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
            }
            if (sortObj.isSorted()) {
                sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
            }
        }

        // Distinct results to prevent duplicates on variants joins
        if (size != null || color != null) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                return cb.conjunction();
            });
        }

        return productRepository.findAll(spec, sortedPageable).map(ProductMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> autocomplete(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return productRepository.findNamesByKeyword(keyword.toLowerCase(), PageRequest.of(0, 10));
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
