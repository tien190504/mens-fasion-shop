package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.domain.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    long countByCouponIdAndUserId(Long couponId, Long userId);
}
