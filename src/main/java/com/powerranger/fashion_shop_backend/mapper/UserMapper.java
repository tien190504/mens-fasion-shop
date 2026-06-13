package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.User;
import com.powerranger.fashion_shop_backend.dto.response.UserResponse;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        if (u == null) return null;
        return new UserResponse(
            u.getId(),
            u.getEmail(),
            u.getPhone(),
            u.getFullName(),
            u.getAvatarUrl(),
            u.getDateOfBirth(),
            u.isActive(),
            u.isAdmin()
        );
    }
}
