package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.PasswordResetToken;
import com.powerranger.fashion_shop_backend.dto.response.PasswordResetTokenResponse;

public final class PasswordResetTokenMapper {
    private PasswordResetTokenMapper() {}

    public static PasswordResetTokenResponse toResponse(PasswordResetToken t) {
        if (t == null) return null;
        return new PasswordResetTokenResponse(
            t.getId(),
            t.getUser() != null ? t.getUser().getId() : null,
            t.getToken(),
            t.getExpiresAt()
        );
    }
}
