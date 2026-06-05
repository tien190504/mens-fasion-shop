package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.CategoryRequest;
import com.powerranger.mens_fashion_backend.dto.response.CategoryResponse;
import com.powerranger.mens_fashion_backend.entity.Category;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.CategoryMapper;
import com.powerranger.mens_fashion_backend.repository.CategoryRepository;
import com.powerranger.mens_fashion_backend.service.CategoryService;
import com.powerranger.mens_fashion_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = SlugUtil.from(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with similar name already exists");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder());
        category.setActive(request.active() != null ? request.active() : true);

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String slug = SlugUtil.from(request.name());
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with similar name already exists");
        }

        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder());
        category.setActive(request.active() != null ? request.active() : true);

        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new BadRequestException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
