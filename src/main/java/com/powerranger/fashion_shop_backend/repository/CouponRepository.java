package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.Coupon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @EntityGraph(attributePaths = {"products", "categories"})
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
