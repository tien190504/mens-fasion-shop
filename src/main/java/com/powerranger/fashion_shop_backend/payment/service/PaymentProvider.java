package com.powerranger.fashion_shop_backend.payment.service;

import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import java.util.Map;

public interface PaymentProvider {
    String createPaymentUrl(ShopOrder order, String transactionRef);
    boolean verifyCallback(Map<String, String> params);
}
