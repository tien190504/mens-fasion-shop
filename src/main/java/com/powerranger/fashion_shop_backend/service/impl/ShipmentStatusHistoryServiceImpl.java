package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.response.ShipmentStatusHistoryResponse;
import com.powerranger.fashion_shop_backend.entity.Shipment;
import com.powerranger.fashion_shop_backend.entity.ShipmentStatusHistory;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.ShipmentStatusHistoryMapper;
import com.powerranger.fashion_shop_backend.repository.ShipmentRepository;
import com.powerranger.fashion_shop_backend.repository.ShipmentStatusHistoryRepository;
import com.powerranger.fashion_shop_backend.service.ShipmentStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentStatusHistoryServiceImpl implements ShipmentStatusHistoryService {

    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentStatusHistoryResponse> getHistory(Long shipmentId) {
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new NotFoundException("Shipment not found");
        }
        return shipmentStatusHistoryRepository.findByShipmentIdOrderByCreatedAtDescIdDesc(shipmentId).stream()
                .map(ShipmentStatusHistoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void addHistory(Long shipmentId, String status, String description) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setShipment(shipment);
        history.setStatus(status);
        history.setDescription(description);
        shipmentStatusHistoryRepository.save(history);
    }
}
