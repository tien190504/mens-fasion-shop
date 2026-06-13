package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.CouponRequest;
import com.powerranger.fashion_shop_backend.dto.response.CouponResponse;
import java.util.List;

public interface CouponService {
    List<CouponResponse> listAll();
    CouponResponse getById(Long id);
    CouponResponse create(CouponRequest request);
    CouponResponse update(Long id, CouponRequest request);
    CouponResponse validateCoupon(String code, String email, java.math.BigDecimal orderAmount);
    void toggleActive(Long id);
}
