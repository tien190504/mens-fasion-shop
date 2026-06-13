package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
