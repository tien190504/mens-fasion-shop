package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.InventoryMovement;
import com.powerranger.fashion_shop_backend.dto.response.MovementResponseDTO;

public final class InventoryMovementMapper {
    private InventoryMovementMapper() {}

    public static MovementResponseDTO toResponse(InventoryMovement m) {
        if (m == null) return null;
        return new MovementResponseDTO(
            m.getId(),
            m.getVariant() != null ? m.getVariant().getId() : null,
            m.getVariant() != null ? m.getVariant().getSku() : null,
            m.getChangeQty(),
            m.getReason() != null ? m.getReason().name() : null,
            m.getOrder() != null ? m.getOrder().getId() : null,
            m.getNote(),
            m.getCreatedAt()
        );
    }
}
