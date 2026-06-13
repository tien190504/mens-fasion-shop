package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.request.CouponRequest;
import com.powerranger.fashion_shop_backend.dto.response.CouponResponse;
import com.powerranger.fashion_shop_backend.entity.Coupon;
import com.powerranger.fashion_shop_backend.entity.enums.DiscountType;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.CouponMapper;
import com.powerranger.fashion_shop_backend.repository.CouponRepository;
import com.powerranger.fashion_shop_backend.repository.CouponUsageRepository;
import com.powerranger.fashion_shop_backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> listAll() {
        return couponRepository.findAll().stream()
                .map(CouponMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getById(Long id) {
        return couponRepository.findById(id)
                .map(CouponMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Coupon not found"));
    }

    @Override
    @Transactional
    public CouponResponse create(CouponRequest request) {
        if (couponRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new BadRequestException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(DiscountType.valueOf(request.discountType().toLowerCase()));
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderAmount(request.minOrderAmount() != null ? request.minOrderAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsageLimitPerUser(request.usageLimitPerUser());
        coupon.setValidFrom(request.validFrom() != null ? request.validFrom() : OffsetDateTime.now());
        coupon.setValidUntil(request.validUntil());
        coupon.setActive(request.active() != null ? request.active() : true);

        return CouponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found"));

        if (!coupon.getCode().equalsIgnoreCase(request.code()) && 
            couponRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new BadRequestException("Coupon code already exists");
        }

        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(DiscountType.valueOf(request.discountType().toLowerCase()));
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderAmount(request.minOrderAmount() != null ? request.minOrderAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsageLimitPerUser(request.usageLimitPerUser());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidUntil(request.validUntil());
        coupon.setActive(request.active() != null ? request.active() : true);

        return CouponMapper.toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse validateCoupon(String code, String email, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new NotFoundException("Coupon code is invalid"));

        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon is not active");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            throw new BadRequestException("Coupon is not yet active");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            throw new BadRequestException("Coupon has expired");
        }

        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount not met");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit exceeded");
        }

        if (coupon.getUsageLimitPerUser() != null && email != null) {
            int usedByUser = couponUsageRepository.countByUserEmailAndCouponId(email, coupon.getId());
            if (usedByUser >= coupon.getUsageLimitPerUser()) {
                throw new BadRequestException("Coupon usage limit per user reached");
            }
        }

        return CouponMapper.toResponse(coupon);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found"));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }
}
