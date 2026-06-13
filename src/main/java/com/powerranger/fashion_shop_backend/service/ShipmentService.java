package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.ShipmentRequest;
import com.powerranger.fashion_shop_backend.dto.response.ShipmentResponse;

public interface ShipmentService {
    ShipmentResponse getByOrder(Long orderId);
    ShipmentResponse create(ShipmentRequest request);
    ShipmentResponse updateStatus(Long shipmentId, String status);
    ShipmentResponse getByTrackingNumber(String trackingNumber);
}
