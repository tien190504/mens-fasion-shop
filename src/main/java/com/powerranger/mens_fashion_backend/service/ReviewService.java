package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.ReviewRequest;
import com.powerranger.mens_fashion_backend.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Page<ReviewResponse> listByProduct(Long productId, Pageable pageable);
    ReviewResponse create(String email, Long productId, ReviewRequest request);
    void delete(String email, Long id);
}
