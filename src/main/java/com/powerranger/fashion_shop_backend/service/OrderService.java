package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.OrderRequest;
import com.powerranger.fashion_shop_backend.dto.request.OrderStatusRequest;
import com.powerranger.fashion_shop_backend.dto.response.OrderResponse;
import com.powerranger.fashion_shop_backend.dto.response.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(String email, OrderRequest request);
    OrderResponse getOrder(Long id, String email);
    Page<OrderResponse> listMyOrders(String email, Pageable pageable);
    Page<OrderResponse> listAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, OrderStatusRequest request);
    InvoiceResponse getInvoice(Long id, String email);
}
