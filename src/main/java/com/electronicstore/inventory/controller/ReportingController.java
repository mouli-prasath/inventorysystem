package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.response.InventoryValuationResponse;
import com.electronicstore.inventory.dto.response.ProfitMarginResponse;
import com.electronicstore.inventory.dto.response.SalesSummaryResponse;
import com.electronicstore.inventory.dto.response.TopSellingProductResponse;
import com.electronicstore.inventory.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FR10: Comprehensive Sales Analytics and Reporting System.
 * FR11: Business Intelligence and Strategic Planning System (foundational metrics).
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/sales/summary")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportingService.getSalesSummary(start, end));
    }

    @GetMapping("/sales/daily")
    public ResponseEntity<SalesSummaryResponse> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportingService.getDailySalesSummary(date));
    }

    @GetMapping("/sales/top-products")
    public ResponseEntity<Page<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportingService.getTopSellingProducts(start, end, limit));
    }

    @GetMapping("/inventory/valuation")
    public ResponseEntity<InventoryValuationResponse> getInventoryValuation() {
        return ResponseEntity.ok(reportingService.getInventoryValuation());
    }

    @GetMapping("/inventory/margins")
    public ResponseEntity<List<ProfitMarginResponse>> getProfitMargins(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean lowestFirst) {
        return ResponseEntity.ok(reportingService.getProfitMargins(limit, lowestFirst));
    }
}
