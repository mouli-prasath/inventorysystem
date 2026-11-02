package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.SupplierRequest;
import com.electronicstore.inventory.dto.response.SupplierResponse;
import com.electronicstore.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Part of FR8: Supplier Management and Purchase Order System.
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(supplierService.getAll(activeOnly));
    }
    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(supplierService.search(name));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
