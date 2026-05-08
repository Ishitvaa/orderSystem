package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
    List<OrderPayment> findByOrderId(Long orderId);
}