package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Shipment;
import com.powerranger.mens_fashion_backend.dto.response.ShipmentResponse;

public final class ShipmentMapper {
    private ShipmentMapper() {}

    public static ShipmentResponse toResponse(Shipment s) {
        if (s == null) return null;
        return new ShipmentResponse(
            s.getId(),
            s.getOrder() != null ? s.getOrder().getId() : null,
            s.getTrackingNumber(),
            s.getCarrier(),
            s.getStatus() != null ? s.getStatus().name() : null,
            s.getShippedAt(),
            s.getEstimatedDelivery()
        );
    }
}
