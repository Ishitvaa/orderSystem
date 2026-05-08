package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.CustomerDTO;
import com.phegondev.InventoryMgtSystem.dtos.OrderDTO;
import com.phegondev.InventoryMgtSystem.dtos.Response;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.Customer;
import com.phegondev.InventoryMgtSystem.repositories.CustomerRepository;
import com.phegondev.InventoryMgtSystem.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response addCustomer(CustomerDTO customerDTO) {
        Customer customer = modelMapper.map(customerDTO, Customer.class);
        customerRepository.save(customer);

        return Response.builder()
                .status(200)
                .message("Customer Added Successfully")
                .build();
    }

    @Override
    public Response getAllCustomers() {
        List<Customer> customers = customerRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"));

        customers.forEach(customer -> customer.setOrders(null));

        List<CustomerDTO> customerDTOs = modelMapper.map(customers,
                new TypeToken<List<CustomerDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .customers(customerDTOs)
                .build();
    }

    @Override
    public Response getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer Not Found"));

        customer.setOrders(null);
        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .customer(customerDTO)
                .build();
    }

    @Override
    public Response updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer Not Found"));

        if (customerDTO.getFirstName() != null)
            existingCustomer.setFirstName(customerDTO.getFirstName());
        if (customerDTO.getLastName() != null)
            existingCustomer.setLastName(customerDTO.getLastName());
        if (customerDTO.getEmail() != null)
            existingCustomer.setEmail(customerDTO.getEmail());
        if (customerDTO.getPhone() != null)
            existingCustomer.setPhone(customerDTO.getPhone());
        if (customerDTO.getCompanyName() != null)
            existingCustomer.setCompanyName(customerDTO.getCompanyName());
        if (customerDTO.getCustomerType() != null)
            existingCustomer.setCustomerType(customerDTO.getCustomerType());
        if (customerDTO.getNotes() != null)
            existingCustomer.setNotes(customerDTO.getNotes());
        if (customerDTO.getIsActive() != null)
            existingCustomer.setIsActive(customerDTO.getIsActive());

        customerRepository.save(existingCustomer);

        return Response.builder()
                .status(200)
                .message("Customer Updated Successfully")
                .build();
    }

    @Override
    public Response deleteCustomer(Long id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer Not Found"));

        customerRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Customer Deleted Successfully")
                .build();
    }

    @Override
    public Response searchCustomers(String searchValue) {
        List<Customer> customers = customerRepository
                .findByFirstNameContainingOrLastNameContaining(
                        searchValue, searchValue);

        customers.forEach(customer -> customer.setOrders(null));

        List<CustomerDTO> customerDTOs = modelMapper.map(customers,
                new TypeToken<List<CustomerDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .customers(customerDTOs)
                .build();
    }

    @Override
    public Response getCustomerOrders(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer Not Found"));

        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);

        if (customerDTO.getOrders() != null) {
            customerDTO.getOrders().forEach(orderDTO -> {
                orderDTO.setCustomer(null);
                orderDTO.setUser(null);
            });
        }

        return Response.builder()
                .status(200)
                .message("success")
                .customer(customerDTO)
                .build();
    }
}