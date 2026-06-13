package com.powerranger.fashion_shop_backend.payment.service;

import com.powerranger.fashion_shop_backend.payment.dto.PaymentRequest;
import com.powerranger.fashion_shop_backend.payment.dto.PaymentResponse;
import java.util.Map;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    void processCallback(Map<String, String> params);
    void processIpn(Map<String, String> params);
}
