package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.Address;
import com.powerranger.fashion_shop_backend.dto.response.AddressResponse;

public final class AddressMapper {
    private AddressMapper() {}

    public static AddressResponse toResponse(Address a) {
        if (a == null) return null;
        return new AddressResponse(
            a.getId(),
            a.getRecipientName(),
            a.getPhone(),
            a.getProvince(),
            a.getDistrict(),
            a.getWard(),
            a.getStreetAddress(),
            a.isDefault()
        );
    }
}
