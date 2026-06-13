package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.response.ShipmentStatusHistoryResponse;
import java.util.List;

public interface ShipmentStatusHistoryService {
    List<ShipmentStatusHistoryResponse> getHistory(Long shipmentId);
    void addHistory(Long shipmentId, String status, String description);
}
