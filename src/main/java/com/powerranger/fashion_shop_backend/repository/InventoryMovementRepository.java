package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    long countByVariantId(Long variantId);
    List<InventoryMovement> findByVariantId(Long variantId);
}
