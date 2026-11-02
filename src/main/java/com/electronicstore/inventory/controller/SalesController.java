package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.response.SalesSummaryResponse;
import com.electronicstore.inventory.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Appendix H (Sales Management section): GET /sales/daily. Thin alias over
 * {@link ReportingService} so the exact path from the spec resolves, alongside the richer
 * /api/reports/* surface used for everything else.
 */
@RestController
@RequiredArgsConstructor
public class SalesController {
    private final ReportingService reportingService;
    @GetMapping("/api/sales/daily")
    public ResponseEntity<SalesSummaryResponse> getDailySales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportingService.getDailySalesSummary(date != null ? date : LocalDate.now()));
    }
}
