package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.CartItemRequest;
import com.powerranger.fashion_shop_backend.dto.response.CartResponse;
import com.powerranger.fashion_shop_backend.entity.Cart;
import com.powerranger.fashion_shop_backend.entity.CartItem;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.entity.User;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.repository.CartItemRepository;
import com.powerranger.fashion_shop_backend.repository.CartRepository;
import com.powerranger.fashion_shop_backend.repository.ProductVariantRepository;
import com.powerranger.fashion_shop_backend.repository.UserRepository;
import com.powerranger.fashion_shop_backend.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart userCart;
    private Cart guestCart;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        variant = new ProductVariant();
        variant.setId(100L);
        variant.setSku("SKU-100");
        variant.setPrice(BigDecimal.valueOf(100000));
        variant.setStockQuantity(10);

        userCart = new Cart();
        userCart.setId(10L);
        userCart.setUser(user);
        userCart.setItems(new ArrayList<>());

        guestCart = new Cart();
        guestCart.setId(20L);
        guestCart.setSessionToken("session-abc");
        guestCart.setItems(new ArrayList<>());
    }

    @Test
    void getOrCreateUserCartSuccessfully() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(userCart));

        CartResponse response = cartService.getCart("user@example.com", null);

        assertNotNull(response);
        assertEquals(10L, response.id());
    }

    @Test
    void getOrCreateGuestCartAndMergeSuccessfullyOnLogin() {
        // Prepare guest cart item
        CartItem guestItem = new CartItem();
        guestItem.setId(5L);
        guestItem.setCart(guestCart);
        guestItem.setVariant(variant);
        guestItem.setQuantity(2);
        guestCart.getItems().add(guestItem);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(userCart));
        when(cartRepository.findBySessionToken("session-abc")).thenReturn(Optional.of(guestCart));

        CartResponse response = cartService.getCart("user@example.com", "session-abc");

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        verify(cartRepository, times(1)).delete(guestCart);
    }

    @Test
    void addItemToGuestCartSuccessfully() {
        CartItemRequest request = new CartItemRequest(100L, 3);
        when(cartRepository.findBySessionToken("session-abc")).thenReturn(Optional.of(guestCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.of(variant));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(null, "session-abc", request);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
    }

    @Test
    void addItemThrowsWhenStockIsInsufficient() {
        CartItemRequest request = new CartItemRequest(100L, 12); // stock is 10
        when(cartRepository.findBySessionToken("session-abc")).thenReturn(Optional.of(guestCart));
        when(productVariantRepository.findById(100L)).thenReturn(Optional.of(variant));

        assertThrows(BadRequestException.class, () -> cartService.addItem(null, "session-abc", request));
    }
}
