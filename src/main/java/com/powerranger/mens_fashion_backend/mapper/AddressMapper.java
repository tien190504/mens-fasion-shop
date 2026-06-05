package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.Address;
import com.powerranger.mens_fashion_backend.dto.response.AddressResponse;

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
