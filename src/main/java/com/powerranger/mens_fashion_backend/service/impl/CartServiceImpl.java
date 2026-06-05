package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.CartItemRequest;
import com.powerranger.mens_fashion_backend.dto.response.CartResponse;
import com.powerranger.mens_fashion_backend.entity.Cart;
import com.powerranger.mens_fashion_backend.entity.CartItem;
import com.powerranger.mens_fashion_backend.entity.ProductVariant;
import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.CartMapper;
import com.powerranger.mens_fashion_backend.repository.CartItemRepository;
import com.powerranger.mens_fashion_backend.repository.CartRepository;
import com.powerranger.mens_fashion_backend.repository.ProductVariantRepository;
import com.powerranger.mens_fashion_backend.repository.UserRepository;
import com.powerranger.mens_fashion_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public CartResponse getCart(String email, String sessionToken) {
        Cart cart = getOrCreateCart(email, sessionToken);
        // If guest cart with session token exists and user logs in, merge them
        if (email != null && sessionToken != null) {
            Optional<Cart> guestCartOpt = cartRepository.findBySessionToken(sessionToken);
            if (guestCartOpt.isPresent()) {
                Cart guestCart = guestCartOpt.get();
                mergeCarts(guestCart, cart);
                cartRepository.delete(guestCart);
            }
        }
        return CartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String email, String sessionToken, CartItemRequest request) {
        Cart cart = getOrCreateCart(email, sessionToken);
        ProductVariant variant = productVariantRepository.findById(request.variantId())
                .orElseThrow(() -> new NotFoundException("Product variant not found"));

        if (variant.getStockQuantity() < request.quantity()) {
            throw new BadRequestException("Not enough stock available");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(request.variantId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.quantity();
            if (variant.getStockQuantity() < newQty) {
                throw new BadRequestException("Not enough stock available");
            }
            item.setQuantity(newQty);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(request.quantity());
            cart.getItems().add(item);
        }

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String email, String sessionToken, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(email, sessionToken);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        if (item.getVariant().getStockQuantity() < quantity) {
            throw new BadRequestException("Not enough stock available");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return CartMapper.toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(String email, String sessionToken, Long itemId) {
        Cart cart = getOrCreateCart(email, sessionToken);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(String email, String sessionToken) {
        Cart cart = getOrCreateCart(email, sessionToken);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String email, String sessionToken) {
        if (email != null) {
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setUser(user);
                        return cartRepository.save(cart);
                    });
        } else if (sessionToken != null) {
            return cartRepository.findBySessionToken(sessionToken)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setSessionToken(sessionToken);
                        cart.setExpiresAt(OffsetDateTime.now().plusDays(30));
                        return cartRepository.save(cart);
                    });
        }
        throw new BadRequestException("Either email or session token is required");
    }

    private void mergeCarts(Cart src, Cart dest) {
        for (CartItem srcItem : src.getItems()) {
            Optional<CartItem> destItemOpt = dest.getItems().stream()
                    .filter(item -> item.getVariant().getId().equals(srcItem.getVariant().getId()))
                    .findFirst();

            if (destItemOpt.isPresent()) {
                CartItem destItem = destItemOpt.get();
                destItem.setQuantity(destItem.getQuantity() + srcItem.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(dest);
                newItem.setVariant(srcItem.getVariant());
                newItem.setQuantity(srcItem.getQuantity());
                dest.getItems().add(newItem);
            }
        }
    }
}
