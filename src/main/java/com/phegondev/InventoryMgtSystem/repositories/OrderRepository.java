package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.enums.OrderStatus;
import com.phegondev.InventoryMgtSystem.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    List<Order> findByUserId(Long userId);
}