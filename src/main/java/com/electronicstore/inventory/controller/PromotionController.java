package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.PromotionRequest;
import com.electronicstore.inventory.dto.response.PromotionResponse;
import com.electronicstore.inventory.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FR4/scope: "Price management and promotional system".
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;
    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.update(id, request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAll() {
        return ResponseEntity.ok(promotionService.getAll());
    }
    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getCurrentlyRunning() {
        return ResponseEntity.ok(promotionService.getCurrentlyRunning());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        promotionService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
