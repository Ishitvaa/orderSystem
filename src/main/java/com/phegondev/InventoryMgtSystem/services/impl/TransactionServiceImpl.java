package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.Response;
import com.phegondev.InventoryMgtSystem.dtos.TransactionDTO;
import com.phegondev.InventoryMgtSystem.dtos.TransactionRequest;
import com.phegondev.InventoryMgtSystem.enums.TransactionStatus;
import com.phegondev.InventoryMgtSystem.enums.TransactionType;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.TransactionService;
import com.phegondev.InventoryMgtSystem.services.UserService;
import com.phegondev.InventoryMgtSystem.specification.TransactionFilter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response purchase(TransactionRequest transactionRequest) {
        if (transactionRequest.getSupplierId() == null)
            throw new NameValueRequiredException("Supplier Id is Required");

        Product product = productRepository.findById(transactionRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));
        Supplier supplier = supplierRepository.findById(transactionRequest.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));
        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() + transactionRequest.getQuantity());
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .product(product).user(user).supplier(supplier)
                .totalProducts(transactionRequest.getQuantity())
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(transactionRequest.getQuantity())))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);
        return Response.builder().status(200).message("Purchase Made successfully").build();
    }

    @Override
    public Response sell(TransactionRequest transactionRequest) {
        Product product = productRepository.findById(transactionRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));
        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - transactionRequest.getQuantity());
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .product(product).user(user)
                .totalProducts(transactionRequest.getQuantity())
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(transactionRequest.getQuantity())))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);
        return Response.builder().status(200).message("Product Sale successfully made").build();
    }

    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {
        if (transactionRequest.getSupplierId() == null)
            throw new NameValueRequiredException("Supplier Id is Required");

        Product product = productRepository.findById(transactionRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));
        Supplier supplier = supplierRepository.findById(transactionRequest.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));
        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - transactionRequest.getQuantity());
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)
                .product(product).user(user).supplier(supplier)
                .totalProducts(transactionRequest.getQuantity())
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);
        return Response.builder().status(200).message("Product Returned in progress").build();
    }

    @Override
    public Response getAllTransactions(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<Transaction> spec = TransactionFilter.byFilter(filter);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransactionDTO> transactionDTOS = modelMapper.map(
                transactionPage.getContent(), new TypeToken<List<TransactionDTO>>() {}.getType());
        transactionDTOS.forEach(t -> { t.setUser(null); t.setProduct(null); t.setSupplier(null); });

        return Response.builder().status(200).message("success")
                .transactions(transactionDTOS)
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .build();
    }

    @Override
    public Response getAllTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));
        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
        if (transactionDTO.getUser() != null) transactionDTO.getUser().setTransactions(null);
        return Response.builder().status(200).message("success").transaction(transactionDTO).build();
    }

    @Override
    public Response getAllTransactionByMonthAndYear(int month, int year) {
        List<Transaction> transactions = transactionRepository.findAll(TransactionFilter.byMonthAndYear(month, year));
        List<TransactionDTO> transactionDTOS = modelMapper.map(transactions, new TypeToken<List<TransactionDTO>>() {}.getType());
        transactionDTOS.forEach(t -> { t.setUser(null); t.setProduct(null); t.setSupplier(null); });
        return Response.builder().status(200).message("success").transactions(transactionDTOS).build();
    }

    @Override
    public Response updateTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));
        existingTransaction.setStatus(status);
        existingTransaction.setUpdateAt(LocalDateTime.now());
        transactionRepository.save(existingTransaction);
        return Response.builder().status(200).message("Transaction Status Successfully Updated").build();
    }
}