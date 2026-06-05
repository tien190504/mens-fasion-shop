package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.CouponRequest;
import com.powerranger.mens_fashion_backend.dto.response.CouponResponse;
import java.util.List;

public interface CouponService {
    List<CouponResponse> listAll();
    CouponResponse getById(Long id);
    CouponResponse create(CouponRequest request);
    CouponResponse update(Long id, CouponRequest request);
    CouponResponse validateCoupon(String code, String email, java.math.BigDecimal orderAmount);
    void toggleActive(Long id);
}
