package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.ShipmentRequest;
import com.powerranger.mens_fashion_backend.dto.response.ShipmentResponse;
import com.powerranger.mens_fashion_backend.entity.ShopOrder;
import com.powerranger.mens_fashion_backend.entity.Shipment;
import com.powerranger.mens_fashion_backend.entity.enums.ShipmentStatus;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.ShipmentMapper;
import com.powerranger.mens_fashion_backend.repository.OrderRepository;
import com.powerranger.mens_fashion_backend.repository.ShipmentRepository;
import com.powerranger.mens_fashion_backend.service.ShipmentService;
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

        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
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

        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
    }
}
