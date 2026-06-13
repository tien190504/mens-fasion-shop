package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.OrderRequest;
import com.powerranger.fashion_shop_backend.dto.request.OrderStatusRequest;
import com.powerranger.fashion_shop_backend.dto.response.OrderResponse;
import com.powerranger.fashion_shop_backend.entity.*;
import com.powerranger.fashion_shop_backend.entity.enums.*;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.repository.*;
import com.powerranger.fashion_shop_backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CouponUsageRepository couponUsageRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private InventoryMovementRepository inventoryMovementRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock private AddressRepository addressRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private ProductVariant variant;
    private ShopOrder order;
    private Address address;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        Product product = new Product();
        product.setId(2L);
        product.setName("Product Name");

        variant = new ProductVariant();
        variant.setId(10L);
        variant.setSku("SKU-10");
        variant.setPrice(BigDecimal.valueOf(100000));
        variant.setStockQuantity(10);
        variant.setProduct(product);

        cart = new Cart();
        cart.setId(5L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setVariant(variant);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);

        address = new Address();
        address.setId(20L);
        address.setUser(user);
        address.setRecipientName("John Doe");
        address.setPhone("0987654321");
        address.setStreetAddress("123 Street");
        address.setDistrict("District 1");
        address.setProvince("HCM");

        order = new ShopOrder();
        order.setId(100L);
        order.setOrderCode("ORD-12345");
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(BigDecimal.valueOf(200000));
        order.setTotalAmount(BigDecimal.valueOf(230000));
        order.setItems(new ArrayList<>());
        
        OrderItem orderItem = new OrderItem();
        orderItem.setId(50L);
        orderItem.setOrder(order);
        orderItem.setVariant(variant);
        orderItem.setQuantity(2);
        order.getItems().add(orderItem);
    }

    @Test
    void placeOrderWithRawAddressSuccessfully() {
        OrderRequest request = new OrderRequest(
                "John Doe", "0987654321", "123 Street, HCM", "cod", null, "Please call", null);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productVariantRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(ShopOrder.class))).thenAnswer(invocation -> {
            ShopOrder o = invocation.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrderResponse response = orderService.placeOrder("user@example.com", request);

        assertNotNull(response);
        assertEquals("John Doe", response.recipientName());
        assertEquals("PENDING", response.status());
        assertEquals(8, variant.getStockQuantity()); // stock was 10, ordered 2
        verify(inventoryMovementRepository, times(1)).saveAll(any());
        verify(orderStatusHistoryRepository, times(1)).save(any());
    }

    @Test
    void placeOrderWithAddressIdSuccessfully() {
        OrderRequest request = new OrderRequest(
                null, null, null, "cod", null, "Please call", 20L);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));
        when(productVariantRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(ShopOrder.class))).thenAnswer(invocation -> {
            ShopOrder o = invocation.getArgument(0);
            o.setId(101L);
            return o;
        });

        OrderResponse response = orderService.placeOrder("user@example.com", request);

        assertNotNull(response);
        assertEquals("John Doe", response.recipientName());
        assertTrue(response.shippingAddress().contains("123 Street"));
        assertEquals("PENDING", response.status());
    }

    @Test
    void updateOrderStatusTransitionApproved() {
        OrderStatusRequest request = new OrderStatusRequest("CONFIRMED", "Confirmed order");
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(ShopOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(100L, request);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        verify(orderStatusHistoryRepository, times(1)).save(any());
    }

    @Test
    void updateOrderStatusTransitionDenied() {
        OrderStatusRequest request = new OrderStatusRequest("DELIVERED", "Jump state directly");
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus(100L, request));
    }

    @Test
    void cancelOrderTriggersStockRecovery() {
        OrderStatusRequest request = new OrderStatusRequest("CANCELLED", "Client request");
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(order));
        when(productVariantRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(ShopOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        variant.setStockQuantity(5); // current stock before cancel

        OrderResponse response = orderService.updateOrderStatus(100L, request);

        assertNotNull(response);
        assertEquals("CANCELLED", response.status());
        assertEquals(7, variant.getStockQuantity()); // 5 + 2 recovered
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }
}
