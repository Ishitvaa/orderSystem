package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.*;
import com.phegondev.InventoryMgtSystem.enums.OrderStatus;
import com.phegondev.InventoryMgtSystem.enums.PaymentStatus;
import com.phegondev.InventoryMgtSystem.exceptions.InsufficientStockException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.OrderService;
import com.phegondev.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer Not Found"));

        User user = userService.getCurrentLoggedInUser();
        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .orderNotes(request.getOrderNotes())
                .internalNotes(request.getInternalNotes())
                .paymentMethod(request.getPaymentMethod())
                .build();

        order = orderRepository.save(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : request.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found: " + itemDTO.getProductId()));

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            BigDecimal itemDiscount = itemDTO.getDiscount() != null ? itemDTO.getDiscount() : BigDecimal.ZERO;
            BigDecimal unitPrice = itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : product.getPrice();
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())).subtract(itemDiscount);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .discount(itemDiscount)
                    .subtotal(itemSubtotal)
                    .notes(itemDTO.getNotes())
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(itemSubtotal);

            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);
        }

        orderItemRepository.saveAll(orderItems);

        BigDecimal total = subtotal
                .add(order.getTaxAmount())
                .add(order.getShippingCost())
                .subtract(order.getDiscountAmount());

        order.setSubtotal(subtotal);
        order.setTotalAmount(total);
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        return Response.builder().status(200).message("Order Created Successfully").build();
    }

    @Override
    public Response getAllOrders(String filter) {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<OrderDTO> orderDTOs = modelMapper.map(orders, new TypeToken<List<OrderDTO>>() {}.getType());

        orderDTOs.forEach(orderDTO -> {
            orderDTO.setOrderItems(null);
            orderDTO.setPayments(null);
        });

        if (filter != null && !filter.isEmpty()) {
            String lowerFilter = filter.toLowerCase();
            orderDTOs = orderDTOs.stream()
                    .filter(o ->
                        (o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(lowerFilter)) ||
                        (o.getOrderStatus() != null && o.getOrderStatus().toString().toLowerCase().contains(lowerFilter)) ||
                        (o.getPaymentStatus() != null && o.getPaymentStatus().toString().toLowerCase().contains(lowerFilter)) ||
                        (o.getCustomer() != null && o.getCustomer().getFirstName() != null &&
                            o.getCustomer().getFirstName().toLowerCase().contains(lowerFilter)) ||
                        (o.getCustomer() != null && o.getCustomer().getLastName() != null &&
                            o.getCustomer().getLastName().toLowerCase().contains(lowerFilter))
                    ).toList();
        }

        return Response.builder().status(200).message("success").orders(orderDTOs).build();
    }

    @Override
    public Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        if (orderDTO.getUser() != null) orderDTO.getUser().setTransactions(null);
        if (orderDTO.getCustomer() != null) orderDTO.getCustomer().setOrders(null);
        return Response.builder().status(200).message("success").order(orderDTO).build();
    }

    @Override
    public Response getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        return Response.builder().status(200).message("success").order(orderDTO).build();
    }

    @Override
    public Response getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        List<OrderDTO> orderDTOs = modelMapper.map(orders, new TypeToken<List<OrderDTO>>() {}.getType());
        orderDTOs.forEach(o -> { o.setOrderItems(null); o.setPayments(null); });
        return Response.builder().status(200).message("success").orders(orderDTOs).build();
    }

    @Override
    public Response getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatus(status);
        List<OrderDTO> orderDTOs = modelMapper.map(orders, new TypeToken<List<OrderDTO>>() {}.getType());
        return Response.builder().status(200).message("success").orders(orderDTOs).build();
    }

    @Override
    @Transactional
    public Response updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        OrderStatus oldStatus = order.getOrderStatus();

        order.setOrderStatus(newStatus);
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> { order.setCancelledAt(LocalDateTime.now()); restoreStock(order); }
            default -> {}
        }
        orderRepository.save(order);

        User currentUser = userService.getCurrentLoggedInUser();
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).user(currentUser).oldStatus(oldStatus).newStatus(newStatus).notes(notes).build();
        orderStatusHistoryRepository.save(history);

        return Response.builder().status(200).message("Order Status Updated Successfully").build();
    }

    @Override
    @Transactional
    public Response cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        restoreStock(order);
        orderRepository.save(order);
        return Response.builder().status(200).message("Order Cancelled Successfully").build();
    }

    @Override
    @Transactional
    public Response confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);
        return Response.builder().status(200).message("Order Confirmed Successfully").build();
    }

    @Override
    @Transactional
    public Response shipOrder(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        order.setTrackingNumber(trackingNumber);
        orderRepository.save(order);
        return Response.builder().status(200).message("Order Shipped Successfully").build();
    }

    @Override
    @Transactional
    public Response deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
        return Response.builder().status(200).message("Order Delivered Successfully").build();
    }

    private String generateOrderNumber() {
        String prefix = "ORD-" + LocalDate.now().getYear() + "-";
        Long count = orderRepository.count() + 1;
        return prefix + String.format("%05d", count);
    }

    private void restoreStock(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }
}