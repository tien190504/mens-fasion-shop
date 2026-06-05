package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.OrderRequest;
import com.powerranger.mens_fashion_backend.dto.request.OrderStatusRequest;
import com.powerranger.mens_fashion_backend.dto.response.OrderResponse;
import com.powerranger.mens_fashion_backend.entity.*;
import com.powerranger.mens_fashion_backend.entity.enums.*;
import com.powerranger.mens_fashion_backend.exception.BadRequestException;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.OrderMapper;
import com.powerranger.mens_fashion_backend.repository.*;
import com.powerranger.mens_fashion_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            coupon = couponRepository.findByCodeIgnoreCase(request.couponCode())
                    .orElseThrow(() -> new NotFoundException("Coupon not found"));

            // Simple validation
            if (!coupon.isActive()) {
                throw new BadRequestException("Coupon is inactive");
            }
            if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new BadRequestException("Order amount does not meet minimum for coupon");
            }

            if (coupon.getDiscountType() == DiscountType.percentage) {
                discountAmount = subtotal.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
                if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discountAmount = coupon.getMaxDiscountAmount();
                }
            } else {
                discountAmount = coupon.getDiscountValue();
            }
            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal;
            }
        }

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // Flat shipping fee
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(shippingFee);

        ShopOrder order = new ShopOrder();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUser(user);
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setShippingAddress(request.shippingAddress());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.pending);
        order.setPaymentStatus(PaymentStatus.unpaid);
        order.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod().toLowerCase()));
        order.setNote(request.note());
        order.setCoupon(coupon);

        order.setItems(new ArrayList<>());
        List<InventoryMovement> inventoryMovements = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            // Pessimistic write lock for variant stock management
            ProductVariant variant = productVariantRepository.findByIdForUpdate(cartItem.getVariant().getId())
                    .orElseThrow(() -> new NotFoundException("Variant not found"));

            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Not enough stock for SKU: " + variant.getSku());
            }

            // Decrement stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setVariantLabel(variant.getSize() + " / " + variant.getColor());
            orderItem.setSku(variant.getSku());
            orderItem.setUnitPrice(variant.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setLineTotal(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(orderItem);

            // Record inventory movement
            InventoryMovement movement = new InventoryMovement();
            movement.setVariant(variant);
            movement.setChangeQty(-cartItem.getQuantity());
            movement.setReason(InventoryReason.sale);
            movement.setOrder(order);
            movement.setNote("Order purchase: " + order.getOrderCode());
            inventoryMovements.add(movement);
        }

        ShopOrder savedOrder = orderRepository.save(order);
        inventoryMovementRepository.saveAll(inventoryMovements);

        // Coupon usage
        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);

            CouponUsage usage = new CouponUsage();
            usage.setUser(user);
            usage.setCoupon(coupon);
            usage.setOrder(savedOrder);
            couponUsageRepository.save(usage);
        }

        // Record status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(savedOrder);
        history.setNewStatus(OrderStatus.pending);
        history.setNote("Order placed");
        orderStatusHistoryRepository.save(history);

        // Clear Cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ShopOrder order;
        if (user.isAdmin()) {
            order = orderRepository.findWithItemsById(id)
                    .orElseThrow(() -> new NotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NotFoundException("Order not found or access denied"));
        }
        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listMyOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId(), pageable)
                .map(OrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusRequest request) {
        ShopOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(request.status().toLowerCase());
        
        // Stock recovery on cancellation
        if (newStatus == OrderStatus.cancelled && order.getStatus() != OrderStatus.cancelled) {
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = productVariantRepository.findByIdForUpdate(item.getVariant().getId())
                        .orElseThrow(() -> new NotFoundException("Variant not found"));
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productVariantRepository.save(variant);

                InventoryMovement movement = new InventoryMovement();
                movement.setVariant(variant);
                movement.setChangeQty(item.getQuantity());
                movement.setReason(InventoryReason.cancellation);
                movement.setOrder(order);
                movement.setNote("Order cancelled: " + order.getOrderCode());
                inventoryMovementRepository.save(movement);
            }
        }

        order.setStatus(newStatus);
        ShopOrder savedOrder = orderRepository.save(order);

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(savedOrder);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(request.note());
        orderStatusHistoryRepository.save(history);

        return OrderMapper.toResponse(savedOrder);
    }
}
