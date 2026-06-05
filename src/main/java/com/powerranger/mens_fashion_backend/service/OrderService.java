package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.OrderRequest;
import com.powerranger.mens_fashion_backend.dto.request.OrderStatusRequest;
import com.powerranger.mens_fashion_backend.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(String email, OrderRequest request);
    OrderResponse getOrder(Long id, String email);
    Page<OrderResponse> listMyOrders(String email, Pageable pageable);
    Page<OrderResponse> listAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, OrderStatusRequest request);
}
