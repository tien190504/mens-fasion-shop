package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.domain.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    long countByVariantId(Long variantId);
}
