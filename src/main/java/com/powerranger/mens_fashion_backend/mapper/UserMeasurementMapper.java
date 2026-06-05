package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.UserMeasurement;
import com.powerranger.mens_fashion_backend.dto.response.UserMeasurementResponse;

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
