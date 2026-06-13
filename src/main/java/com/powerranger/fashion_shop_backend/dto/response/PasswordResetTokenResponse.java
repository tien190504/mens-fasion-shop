package com.powerranger.fashion_shop_backend.dto.response;

import java.time.OffsetDateTime;

public record PasswordResetTokenResponse(
    Long id,
    Long userId,
    String token,
    OffsetDateTime expiresAt
) {}
