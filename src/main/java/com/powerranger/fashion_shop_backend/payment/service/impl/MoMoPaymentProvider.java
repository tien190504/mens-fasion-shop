package com.powerranger.fashion_shop_backend.payment.service.impl;

import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import com.powerranger.fashion_shop_backend.payment.service.PaymentProvider;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component("momoProvider")
public class MoMoPaymentProvider implements PaymentProvider {
    @Override
    public String createPaymentUrl(ShopOrder order, String transactionRef) {
        return "/api/v1/payments/mock-checkout?orderId=" + order.getId() + 
               "&ref=" + transactionRef + 
               "&amount=" + order.getTotalAmount() + 
               "&method=momo";
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        return "success".equalsIgnoreCase(params.get("status"));
    }
}
