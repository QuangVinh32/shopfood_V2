package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByOrder(Order order);
}
