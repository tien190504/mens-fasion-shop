package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {"items", "items.variant", "items.variant.product", "items.variant.product.brand", "items.variant.product.category"})
    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.variant", "items.variant.product", "items.variant.product.brand", "items.variant.product.category"})
    Optional<Cart> findBySessionToken(String sessionToken);
}
