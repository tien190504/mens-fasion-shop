package com.powerranger.fashion_shop_backend.payment.service.impl;

import com.powerranger.fashion_shop_backend.entity.OrderStatusHistory;
import com.powerranger.fashion_shop_backend.entity.PaymentTransaction;
import com.powerranger.fashion_shop_backend.entity.ShopOrder;
import com.powerranger.fashion_shop_backend.entity.enums.OrderStatus;
import com.powerranger.fashion_shop_backend.entity.enums.PaymentMethod;
import com.powerranger.fashion_shop_backend.entity.enums.PaymentStatus;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.payment.dto.PaymentRequest;
import com.powerranger.fashion_shop_backend.payment.dto.PaymentResponse;
import com.powerranger.fashion_shop_backend.payment.service.PaymentProvider;
import com.powerranger.fashion_shop_backend.payment.service.PaymentService;
import com.powerranger.fashion_shop_backend.repository.OrderRepository;
import com.powerranger.fashion_shop_backend.repository.OrderStatusHistoryRepository;
import com.powerranger.fashion_shop_backend.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentProvider momoProvider;
    private final PaymentProvider vnpayProvider;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        ShopOrder order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.paid) {
            throw new BadRequestException("Order is already paid");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.method().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method: " + request.method());
        }

        String transactionRef = method == PaymentMethod.cod ? "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : request.method().toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentProvider provider = method == PaymentMethod.momo ? momoProvider : vnpayProvider;
        String paymentUrl = "";
        
        if (method == PaymentMethod.cod) {
            paymentUrl = "/api/v1/payments/mock-checkout?orderId=" + order.getId() + 
                         "&ref=" + transactionRef + 
                         "&amount=" + order.getTotalAmount() + 
                         "&method=cod";
        } else {
            paymentUrl = provider.createPaymentUrl(order, transactionRef);
        }

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setMethod(method);
        transaction.setAmount(order.getTotalAmount());
        transaction.setTransactionRef(transactionRef);
        transaction.setStatus(PaymentStatus.unpaid);
        transaction.setGatewayResponse("{}");
        paymentTransactionRepository.save(transaction);

        order.setPaymentMethod(method);
        orderRepository.save(order);

        return new PaymentResponse(paymentUrl, transactionRef);
    }

    @Override
    @Transactional
    public void processCallback(Map<String, String> params) {
        String ref = params.get("ref");
        if (ref == null) {
            throw new BadRequestException("Transaction reference is missing");
        }

        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new NotFoundException("Transaction not found for ref: " + ref));

        if (transaction.getStatus() == PaymentStatus.paid) {
            return; // Already processed
        }

        String status = params.get("status");
        if ("success".equalsIgnoreCase(status)) {
            transaction.setStatus(PaymentStatus.paid);
            paymentTransactionRepository.save(transaction);

            ShopOrder order = transaction.getOrder();
            OrderStatus oldOrderStatus = order.getStatus();
            
            order.setPaymentStatus(PaymentStatus.paid);
            
            // Auto confirm paid orders if they are pending
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                
                OrderStatusHistory history = new OrderStatusHistory();
                history.setOrder(order);
                history.setOldStatus(oldOrderStatus);
                history.setNewStatus(OrderStatus.CONFIRMED);
                history.setNote("Payment completed via " + transaction.getMethod());
                orderStatusHistoryRepository.save(history);
            }
            orderRepository.save(order);
        } else {
            transaction.setStatus(PaymentStatus.failed);
            paymentTransactionRepository.save(transaction);
        }
    }

    @Override
    @Transactional
    public void processIpn(Map<String, String> params) {
        processCallback(params);
    }
}
