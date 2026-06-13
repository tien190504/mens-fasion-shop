package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.response.MovementResponseDTO;
import java.util.List;

public interface InventoryService {
    List<MovementResponseDTO> getMovementsByVariant(Long variantId);
    void addMovement(Long variantId, int quantity, String reason, String note);
}
