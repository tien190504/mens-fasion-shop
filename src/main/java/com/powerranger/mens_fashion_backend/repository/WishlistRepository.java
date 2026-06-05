package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserEmail(String email);
    boolean existsByUserEmailAndProductId(String email, Long productId);
    Optional<Wishlist> findByUserEmailAndProductId(String email, Long productId);
}
