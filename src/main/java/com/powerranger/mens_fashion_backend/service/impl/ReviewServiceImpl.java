package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.ReviewRequest;
import com.powerranger.mens_fashion_backend.dto.response.ReviewResponse;
import com.powerranger.mens_fashion_backend.entity.Product;
import com.powerranger.mens_fashion_backend.entity.Review;
import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.ReviewMapper;
import com.powerranger.mens_fashion_backend.repository.ProductRepository;
import com.powerranger.mens_fashion_backend.repository.ReviewRepository;
import com.powerranger.mens_fashion_backend.repository.UserRepository;
import com.powerranger.mens_fashion_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewMapper::toResponse);
    }

    @Override
    @Transactional
    public ReviewResponse create(String email, Long productId, ReviewRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (reviewRepository.existsByUserEmailAndProductId(email, productId)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating((short) request.rating());
        review.setComment(request.comment());

        return ReviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void delete(String email, Long id) {
        Review review = reviewRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Review not found or not owned by you"));
        reviewRepository.delete(review);
    }
}
