package com.powerranger.fashion_shop_backend.mapper;

import com.powerranger.fashion_shop_backend.entity.Review;
import com.powerranger.fashion_shop_backend.dto.response.ReviewResponse;

public final class ReviewMapper {
    private ReviewMapper() {}

    public static ReviewResponse toResponse(Review r) {
        if (r == null) return null;
        String userName = r.getUser() != null ? r.getUser().getFullName() : "";
        return new ReviewResponse(
            r.getId(),
            userName,
            r.getRating(),
            r.getComment(),
            r.getCreatedAt()
        );
    }
}
