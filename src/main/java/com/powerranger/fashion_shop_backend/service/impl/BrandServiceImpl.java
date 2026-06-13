package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.BrandRequest;
import com.powerranger.fashion_shop_backend.dto.response.BrandResponse;
import com.powerranger.fashion_shop_backend.entity.Brand;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.BrandMapper;
import com.powerranger.fashion_shop_backend.repository.BrandRepository;
import com.powerranger.fashion_shop_backend.service.BrandService;
import com.powerranger.fashion_shop_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> listAll() {
        return brandRepository.findAll().stream()
                .map(BrandMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBySlug(String slug) {
        return brandRepository.findBySlug(slug)
                .map(BrandMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Brand not found"));
    }

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        String slug = SlugUtil.from(request.name());
        if (brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Brand with similar name already exists");
        }

        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setSlug(slug);
        brand.setLogoUrl(request.logoUrl());
        brand.setDescription(request.description());

        return BrandMapper.toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        String slug = SlugUtil.from(request.name());
        if (!brand.getSlug().equals(slug) && brandRepository.existsBySlug(slug)) {
            throw new BadRequestException("Brand with similar name already exists");
        }

        brand.setName(request.name());
        brand.setSlug(slug);
        brand.setLogoUrl(request.logoUrl());
        brand.setDescription(request.description());

        return BrandMapper.toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new NotFoundException("Brand not found");
        }
        brandRepository.deleteById(id);
    }
}
