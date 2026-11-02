package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.ReturnRequest;
import com.electronicstore.inventory.dto.request.TransactionRequest;
import com.electronicstore.inventory.dto.response.ReturnResponse;
import com.electronicstore.inventory.dto.response.TransactionResponse;
import com.electronicstore.inventory.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * FR6: Point of Sale Integration and Transaction Processing System.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    @PostMapping
    public ResponseEntity<TransactionResponse> processSale(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.processSale(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }
    @GetMapping("/number/{transactionNumber}")
    public ResponseEntity<TransactionResponse> getByNumber(@PathVariable String transactionNumber) {
        return ResponseEntity.ok(transactionService.getByNumber(transactionNumber));
    }
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<TransactionResponse>> getByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        return ResponseEntity.ok(transactionService.getByCustomer(customerId, pageable));
    }
    @GetMapping("/report/daily")
    public ResponseEntity<Page<TransactionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 50, sort = "transactionDate") Pageable pageable) {
        return ResponseEntity.ok(transactionService.getByDateRange(start, end, pageable));
    }

    @PutMapping("/{id}/void")
    public ResponseEntity<TransactionResponse> voidTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.voidTransaction(id));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ReturnResponse> processReturn(@PathVariable Long id, @Valid @RequestBody ReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.processReturn(id, request));
    }
}
