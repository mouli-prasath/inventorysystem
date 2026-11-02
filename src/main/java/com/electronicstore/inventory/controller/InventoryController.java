package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.CycleCountRequest;
import com.electronicstore.inventory.dto.request.ReorderPointRequest;
import com.electronicstore.inventory.dto.request.StockAdjustmentRequest;
import com.electronicstore.inventory.dto.response.InventoryResponse;
import com.electronicstore.inventory.dto.response.ProductResponse;
import com.electronicstore.inventory.service.InventoryService;
import com.electronicstore.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FR5: Real-Time Inventory Tracking and Control System.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final ProductService productService;
    @PostMapping("/adjustment")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(request));
    }
    @PostMapping("/count")
    public ResponseEntity<InventoryResponse> recordCycleCount(@Valid @RequestBody CycleCountRequest request) {
        return ResponseEntity.ok(inventoryService.recordCycleCount(request));
    }
    @GetMapping("/stock")
    public ResponseEntity<Page<InventoryResponse>> getStock(
            @PageableDefault(size = 50, sort = "storeLocation") Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAll(pageable));
    }
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStock());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProduct(productId));
    }

    @GetMapping("/location/{storeLocation}")
    public ResponseEntity<List<InventoryResponse>> getByLocation(@PathVariable String storeLocation) {
        return ResponseEntity.ok(inventoryService.getByLocation(storeLocation));
    }

    /** Appendix H: PUT /inventory/reorder - update a product's reorder point. */
    @PutMapping("/reorder")
    public ResponseEntity<ProductResponse> updateReorderPoint(@Valid @RequestBody ReorderPointRequest request) {
        return ResponseEntity.ok(productService.updateReorderPoint(request.getProductId(), request.getReorderPoint()));
    }
}
