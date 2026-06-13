package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.entity.Shipment;
import com.powerranger.fashion_shop_backend.service.ShippingProvider;
import org.springframework.stereotype.Component;

@Component("ghnShippingProvider")
public class GHNShippingProvider implements ShippingProvider {
    @Override
    public String getName() {
        return "Giao Hàng Nhanh (GHN)";
    }

    @Override
    public String createShippingOrder(Shipment shipment) {
        return "GHN" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String getTrackingStatus(String trackingNumber) {
        return "IN_TRANSIT";
    }
}
