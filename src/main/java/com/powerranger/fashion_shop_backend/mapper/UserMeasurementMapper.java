package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.UserMeasurement;
import com.powerranger.fashion_shop_backend.dto.response.UserMeasurementResponse;

public final class UserMeasurementMapper {
    private UserMeasurementMapper() {}

    public static UserMeasurementResponse toResponse(UserMeasurement m) {
        if (m == null) return null;
        return new UserMeasurementResponse(
            m.getId(),
            m.getHeightCm(),
            m.getWeightKg(),
            m.getChestCm(),
            m.getWaistCm(),
            m.getHipCm(),
            m.getShoulderCm()
        );
    }
}
