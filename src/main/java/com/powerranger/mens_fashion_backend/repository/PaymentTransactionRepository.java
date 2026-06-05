package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
    Optional<PaymentTransaction> findByOrderId(Long orderId);
}
