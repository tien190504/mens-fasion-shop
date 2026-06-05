package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v join fetch v.product p left join fetch p.category left join fetch p.secondaryCategories where v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);
}
