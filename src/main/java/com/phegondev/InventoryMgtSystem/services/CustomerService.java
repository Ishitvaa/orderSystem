package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.CustomerDTO;
import com.phegondev.InventoryMgtSystem.dtos.Response;

public interface CustomerService {
    Response addCustomer(CustomerDTO customerDTO);
    Response updateCustomer(Long id, CustomerDTO customerDTO);
    Response getAllCustomers();
    Response getCustomerById(Long id);
    Response deleteCustomer(Long id);
    Response getCustomerWithOrders(Long id);
}