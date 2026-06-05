package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    long countByOrderId(Long orderId);
}
