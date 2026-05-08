package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.CreateOrderRequest;
import com.phegondev.InventoryMgtSystem.dtos.OrderStatusRequest;
import com.phegondev.InventoryMgtSystem.dtos.Response;
import com.phegondev.InventoryMgtSystem.enums.OrderStatus;
import com.phegondev.InventoryMgtSystem.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Response> createOrder(
            @RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllOrders(
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(orderService.getAllOrders(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Response> getOrderByNumber(
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Response> getOrdersByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Response> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Response> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(
                id, request.getStatus(), request.getNotes()));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<Response> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Response> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<Response> shipOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String trackingNumber) {
        return ResponseEntity.ok(orderService.shipOrder(id, trackingNumber));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<Response> deliverOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deliverOrder(id));
    }
}