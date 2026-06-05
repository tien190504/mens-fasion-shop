package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @EntityGraph(attributePaths = {"brand", "category", "secondaryCategories", "images", "variants"})
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
