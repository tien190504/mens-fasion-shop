package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.ShipmentStatusHistory;
import com.powerranger.fashion_shop_backend.dto.response.ShipmentStatusHistoryResponse;

public final class ShipmentStatusHistoryMapper {
    private ShipmentStatusHistoryMapper() {}

    public static ShipmentStatusHistoryResponse toResponse(ShipmentStatusHistory h) {
        if (h == null) return null;
        return new ShipmentStatusHistoryResponse(
            h.getId(),
            h.getShipment() != null ? h.getShipment().getId() : null,
            h.getStatus(),
            h.getDescription(),
            h.getCreatedAt()
        );
    }
}
