package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
