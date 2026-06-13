package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    long countByOrderId(Long orderId);
}
