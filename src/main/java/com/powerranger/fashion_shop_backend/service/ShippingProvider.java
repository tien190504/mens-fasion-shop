package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.entity.Shipment;

public interface ShippingProvider {
    String getName();
    String createShippingOrder(Shipment shipment);
    String getTrackingStatus(String trackingNumber);
}
