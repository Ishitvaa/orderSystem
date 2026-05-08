package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    // Generic
    private int status;
    private String message;

    // For login
    private String token;
    private UserRole role;
    private String expirationTime;

    // For pagination
    private Integer totalPages;
    private Long totalElements;

    // Existing data outputs
    private UserDTO user;
    private List<UserDTO> users;

    private SupplierDTO supplier;
    private List<SupplierDTO> suppliers;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private ProductDTO product;
    private List<ProductDTO> products;

    private TransactionDTO transaction;
    private List<TransactionDTO> transactions;

    // NEW - Customer data outputs
    private CustomerDTO customer;
    private List<CustomerDTO> customers;

    // NEW - Order data outputs
    private OrderDTO order;
    private List<OrderDTO> orders;

    // NEW - Order Item data outputs
    private OrderItemDTO orderItem;
    private List<OrderItemDTO> orderItems;

    // NEW - Order Payment data outputs
    private OrderPaymentDTO orderPayment;
    private List<OrderPaymentDTO> orderPayments;

    private final LocalDateTime timestamp = LocalDateTime.now();
}