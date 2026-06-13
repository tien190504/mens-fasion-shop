package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    long countByCouponIdAndUserId(Long couponId, Long userId);
    int countByUserEmailAndCouponId(String email, Long couponId);
}
