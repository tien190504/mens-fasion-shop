package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
