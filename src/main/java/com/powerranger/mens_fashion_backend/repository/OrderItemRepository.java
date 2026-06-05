package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
