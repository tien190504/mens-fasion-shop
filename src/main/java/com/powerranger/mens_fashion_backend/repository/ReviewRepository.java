package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    boolean existsByUserEmailAndProductId(String email, Long productId);
    Optional<Review> findByIdAndUserEmail(Long id, String email);
}
