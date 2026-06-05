package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.response.ProductSummaryResponse;
import com.powerranger.mens_fashion_backend.entity.Product;
import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.entity.Wishlist;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.ProductMapper;
import com.powerranger.mens_fashion_backend.repository.ProductRepository;
import com.powerranger.mens_fashion_backend.repository.UserRepository;
import com.powerranger.mens_fashion_backend.repository.WishlistRepository;
import com.powerranger.mens_fashion_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> list(String email) {
        return wishlistRepository.findByUserEmail(email).stream()
                .map(Wishlist::getProduct)
                .map(ProductMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public void add(String email, Long productId) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (wishlistRepository.existsByUserEmailAndProductId(email, productId)) {
            throw new BadRequestException("Product already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void remove(String email, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserEmailAndProductId(email, productId)
                .orElseThrow(() -> new NotFoundException("Wishlist item not found"));
        wishlistRepository.delete(wishlist);
    }
}
