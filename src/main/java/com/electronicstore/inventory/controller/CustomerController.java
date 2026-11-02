package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.CustomerRequest;
import com.electronicstore.inventory.dto.request.LoyaltyAdjustmentRequest;
import com.electronicstore.inventory.dto.response.CustomerResponse;
import com.electronicstore.inventory.dto.response.TransactionResponse;
import com.electronicstore.inventory.service.CustomerService;
import com.electronicstore.inventory.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Part of FR7: Customer Relationship Management System (profile & lookup).
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final TransactionService transactionService;
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(customerService.search(keyword, pageable));
    }
    @GetMapping("/{id}/history")
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable Long id, @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        return ResponseEntity.ok(transactionService.getByCustomer(id, pageable));
    }

    /** Appendix H: POST /customers/{id}/loyalty - manual point adjustment (redemption, goodwill credit, correction). */
    @PostMapping("/{id}/loyalty")
    public ResponseEntity<CustomerResponse> adjustLoyalty(
            @PathVariable Long id, @Valid @RequestBody LoyaltyAdjustmentRequest request) {
        return ResponseEntity.ok(customerService.adjustLoyaltyPoints(id, request.getPointsDelta()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
