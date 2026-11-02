package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.PurchaseOrderRequest;
import com.electronicstore.inventory.dto.request.ReceiveDeliveryRequest;
import com.electronicstore.inventory.dto.response.PurchaseOrderResponse;
import com.electronicstore.inventory.enums.PurchaseOrderStatus;
import com.electronicstore.inventory.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

/**
 * FR8 (Supplier Management and Purchase Order System) and FR9 (Procurement and
 * Order Management System).
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.create(request));
    }
    @PostMapping("/auto-generate")
    public ResponseEntity<List<PurchaseOrderResponse>> autoGenerate(
            @RequestParam(required = false) String storeLocation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.autoGenerateFromLowStock(storeLocation));
    }
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Page<PurchaseOrderResponse>> getBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.getBySupplier(supplierId, pageable));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PurchaseOrderResponse>> getByStatus(
            @PathVariable PurchaseOrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.getByStatus(status, pageable));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.approve(id));
    }

    @PutMapping("/{id}/mark-ordered")
    public ResponseEntity<PurchaseOrderResponse> markOrdered(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.markOrdered(id));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receiveDelivery(
            @PathVariable Long id, @Valid @RequestBody ReceiveDeliveryRequest request) {
        return ResponseEntity.ok(purchaseOrderService.receiveDelivery(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderResponse>> getAll(
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable) {
        return ResponseEntity.ok(purchaseOrderService.getAll(pageable));
    }
}
