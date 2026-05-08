package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.CreateOrderRequest;
import com.phegondev.InventoryMgtSystem.dtos.Response;
import com.phegondev.InventoryMgtSystem.enums.OrderStatus;

public interface OrderService {
    Response createOrder(CreateOrderRequest request);
    Response getAllOrders(String filter);
    Response getOrderById(Long id);
    Response getOrderByNumber(String orderNumber);
    Response getOrdersByCustomer(Long customerId);
    Response getOrdersByStatus(OrderStatus status);
    Response updateOrderStatus(Long orderId, OrderStatus status, String notes);
    Response cancelOrder(Long orderId);
    Response confirmOrder(Long orderId);
    Response shipOrder(Long orderId, String trackingNumber);
    Response deliverOrder(Long orderId);
}