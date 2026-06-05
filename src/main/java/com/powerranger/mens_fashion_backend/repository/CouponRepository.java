package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.Coupon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @EntityGraph(attributePaths = {"products", "categories"})
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
