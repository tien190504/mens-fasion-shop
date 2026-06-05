package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.ShipmentRequest;
import com.powerranger.mens_fashion_backend.dto.response.ShipmentResponse;

public interface ShipmentService {
    ShipmentResponse getByOrder(Long orderId);
    ShipmentResponse create(ShipmentRequest request);
    ShipmentResponse updateStatus(Long shipmentId, String status);
}
