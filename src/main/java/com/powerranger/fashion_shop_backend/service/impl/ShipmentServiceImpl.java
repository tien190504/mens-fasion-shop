package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.ShipmentRequest;
import com.powerranger.fashion_shop_backend.dto.response.ShipmentResponse;
import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import com.powerranger.fashion_shop_backend.entity.Shipment;
import com.powerranger.fashion_shop_backend.entity.ShipmentStatusHistory;
import com.powerranger.fashion_shop_backend.entity.enums.ShipmentStatus;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.ShipmentMapper;
import com.powerranger.fashion_shop_backend.repository.OrderRepository;
import com.powerranger.fashion_shop_backend.repository.ShipmentRepository;
import com.powerranger.fashion_shop_backend.repository.ShipmentStatusHistoryRepository;
import com.powerranger.fashion_shop_backend.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getByOrder(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .map(ShipmentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Shipment not found for order"));
    }

    @Override
    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        ShopOrder order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCarrier(request.carrier());
        shipment.setTrackingNumber(request.trackingNumber());
        shipment.setStatus(ShipmentStatus.pending);
        shipment.setShippingFee(request.shippingFee() != null ? request.shippingFee() : BigDecimal.ZERO);
        shipment.setEstimatedDelivery(LocalDate.now().plusDays(5));

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Record history
        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setShipment(savedShipment);
        history.setStatus("PENDING");
        history.setDescription("Shipment order created");
        shipmentStatusHistoryRepository.save(history);

        return ShipmentMapper.toResponse(savedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, String status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        ShipmentStatus newStatus = ShipmentStatus.valueOf(status.toLowerCase());
        shipment.setStatus(newStatus);
        
        if (newStatus == ShipmentStatus.in_transit) {
            shipment.setShippedAt(OffsetDateTime.now());
        } else if (newStatus == ShipmentStatus.delivered) {
            shipment.setDeliveredAt(OffsetDateTime.now());
        }

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Record history
        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setShipment(savedShipment);
        history.setStatus(newStatus.name().toUpperCase());
        history.setDescription("Shipment status updated to " + newStatus.name().toUpperCase());
        shipmentStatusHistoryRepository.save(history);

        return ShipmentMapper.toResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .map(ShipmentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Shipment not found for tracking number: " + trackingNumber));
    }
}
