package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {
    @EntityGraph(attributePaths = {"items", "items.variant", "coupon"})
    Page<ShopOrder> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.variant", "coupon"})
    Optional<ShopOrder> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"items", "items.variant", "coupon"})
    Optional<ShopOrder> findByIdAndUserId(Long id, Long userId);
}
