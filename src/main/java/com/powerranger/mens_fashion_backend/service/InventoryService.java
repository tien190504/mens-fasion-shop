package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.entity.InventoryMovement;
import java.util.List;

public interface InventoryService {
    List<InventoryMovement> getMovementsByVariant(Long variantId);
    void addMovement(Long variantId, int quantity, String reason, String note);
}
