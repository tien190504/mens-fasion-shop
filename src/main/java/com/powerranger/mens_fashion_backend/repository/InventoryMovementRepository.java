package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    long countByVariantId(Long variantId);
    List<InventoryMovement> findByVariantId(Long variantId);
}
