package com.powerranger.fashion_shop_backend.dto.response;

public record AddressResponse(
    Long id,
    String recipientName,
    String phone,
    String province,
    String district,
    String ward,
    String streetAddress,
    boolean isDefault
) {}
