package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
