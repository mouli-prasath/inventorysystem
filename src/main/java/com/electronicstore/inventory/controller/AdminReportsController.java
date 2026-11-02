package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.response.InventoryValuationResponse;
import com.electronicstore.inventory.dto.response.SalesSummaryResponse;
import com.electronicstore.inventory.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Appendix H (Administrative section): GET /admin/analytics, GET /admin/reports.
 * Admin-only per Appendix A ("System Configuration" / full administrative access row);
 * enforced by the general /api/admin/** rule in SecurityConfig.
 */
@RestController
@RequiredArgsConstructor
public class AdminReportsController {

    private final ReportingService reportingService;

    /**
     * A single-call executive snapshot: last-30-days sales performance plus current
     * inventory valuation. For anything more granular, the dedicated /api/reports/* endpoints
     * (sales summary for an arbitrary range, top sellers, per-product margins) give finer control.
     */
    @GetMapping("/api/admin/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);

        SalesSummaryResponse salesLast30Days = reportingService.getSalesSummary(start, end);
        InventoryValuationResponse inventoryValuation = reportingService.getInventoryValuation();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("salesLast30Days", salesLast30Days);
        analytics.put("inventoryValuation", inventoryValuation);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Directory of available report resources, so a client (or an admin exploring the API)
     * can discover what's available without hardcoding every /api/reports/* path.
     */
    @GetMapping("/api/admin/reports")
    public ResponseEntity<List<Map<String, String>>> getAvailableReports() {
        List<Map<String, String>> reports = List.of(
                reportEntry("sales-summary", "Sales totals for an arbitrary date range", "/api/reports/sales/summary?start=&end="),
                reportEntry("sales-daily", "Sales totals for a single day", "/api/reports/sales/daily?date="),
                reportEntry("top-products", "Best sellers by quantity sold in a period", "/api/reports/sales/top-products?start=&end=&limit="),
                reportEntry("inventory-valuation", "Inventory value at cost vs. retail, by category", "/api/reports/inventory/valuation"),
                reportEntry("profit-margins", "Per-product profit margins", "/api/reports/inventory/margins?limit=&lowestFirst="),
                reportEntry("audit-log", "General audit trail", "/api/admin/audit-logs")
        );
        return ResponseEntity.ok(reports);
    }

    private Map<String, String> reportEntry(String id, String description, String path) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("description", description);
        entry.put("path", path);
        return entry;
    }
}
