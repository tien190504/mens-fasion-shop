package com.powerranger.mens_fashion_backend.mapper;

import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.dto.response.UserResponse;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        if (u == null) return null;
        return new UserResponse(
            u.getId(),
            u.getEmail(),
            u.getPhone(),
            u.getFullName(),
            u.isActive(),
            u.isAdmin()
        );
    }
}
