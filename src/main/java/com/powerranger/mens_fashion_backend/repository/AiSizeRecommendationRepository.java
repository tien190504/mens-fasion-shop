package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.AiSizeRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiSizeRecommendationRepository extends JpaRepository<AiSizeRecommendation, Long> {
    Optional<AiSizeRecommendation> findByUserEmailAndProductId(String email, Long productId);
}
