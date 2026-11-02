package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.response.*;
import com.electronicstore.inventory.entity.Product;
import com.electronicstore.inventory.repository.ProductRepository;
import com.electronicstore.inventory.repository.TransactionItemRepository;
import com.electronicstore.inventory.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * FR10: Comprehensive Sales Analytics and Reporting System.
 * FR11: Business Intelligence and Strategic Planning System (foundational metrics; the
 * predictive/ML-driven pieces of FR11 are future-roadmap items per Appendix G).
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final ProductRepository productRepository;

    /** Aggregate revenue/tax/discount/transaction-count/AOV for an arbitrary period. */
    public SalesSummaryResponse getSalesSummary(LocalDateTime start, LocalDateTime end) {
        // Aggregate query has no GROUP BY, so it always returns exactly one row -
        // but Hibernate returns it as List<Object[]> of size 1, not a bare
        // Object[] (see repository Javadoc), so unwrap with .get(0) here.
        Object[] row = transactionRepository.getSalesAggregate(start, end).get(0);

        // sum(...) over zero matching rows returns SQL/JPQL NULL, not zero -
        // handle that here in Java rather than via JPQL coalesce() against a
        // bare "0.0" double literal, which mismatches the BigDecimal column type.
        BigDecimal totalRevenue = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
        BigDecimal totalTax = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
        BigDecimal totalDiscount = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        long count = (Long) row[3];

        BigDecimal averageOrderValue = count == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        return SalesSummaryResponse.builder()
                .periodStart(start)
                .periodEnd(end)
                .totalRevenue(totalRevenue)
                .totalTax(totalTax)
                .totalDiscount(totalDiscount)
                .transactionCount(count)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    /** Convenience wrapper: sales summary for a single calendar day. */
    public SalesSummaryResponse getDailySalesSummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        return getSalesSummary(start, end);
    }

    /** Best sellers by quantity sold within a period. */
    public Page<TopSellingProductResponse> getTopSellingProducts(LocalDateTime start, LocalDateTime end, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return transactionItemRepository.findTopSellingProducts(start, end, pageable);
    }

    /** Current inventory value at cost and at retail, overall and broken down by category. */
    public InventoryValuationResponse getInventoryValuation() {
        // Same list-wrapping unwrap as getSalesSummary above.
        Object[] totals = productRepository.getInventoryValuationAggregate().get(0);

        BigDecimal costValue = totals[0] != null ? (BigDecimal) totals[0] : BigDecimal.ZERO;
        BigDecimal retailValue = totals[1] != null ? (BigDecimal) totals[1] : BigDecimal.ZERO;
        Long totalUnits = (Long) totals[2];
        Long productCount = (Long) totals[3];

        List<CategoryValuationResponse> byCategory = productRepository.getInventoryValuationByCategory().stream()
                .map(row -> CategoryValuationResponse.builder()
                        .categoryName((String) row[0])
                        .costValue(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .retailValue(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                        .totalUnits((Long) row[3])
                        .build())
                .toList();

        return InventoryValuationResponse.builder()
                .totalCostValue(costValue)
                .totalRetailValue(retailValue)
                .potentialProfit(retailValue.subtract(costValue))
                .totalUnits(totalUnits)
                .activeProductCount(productCount)
                .byCategory(byCategory)
                .build();
    }

    /**
     * Per-product profit margins, sorted so the caller can take the strongest or weakest
     * performers. {@code lowestFirst=true} surfaces thin/negative-margin products worth a
     * pricing review.
     */
    public List<ProfitMarginResponse> getProfitMargins(int limit, boolean lowestFirst) {
        List<ProfitMarginResponse> margins = productRepository.findByIsActiveTrue(Pageable.unpaged()).stream()
                .map(this::toMarginResponse)
                .sorted(lowestFirst
                        ? Comparator.comparing(ProfitMarginResponse::getMarginPercent)
                        : Comparator.comparing(ProfitMarginResponse::getMarginPercent).reversed())
                .limit(Math.max(limit, 1))
                .toList();
        return margins;
    }

    private ProfitMarginResponse toMarginResponse(Product p) {
        BigDecimal marginAmount = p.getPrice().subtract(p.getCostPrice());
        BigDecimal marginPercent = p.getPrice().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : marginAmount.divide(p.getPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return ProfitMarginResponse.builder()
                .productId(p.getId())
                .productName(p.getName())
                .sku(p.getSku())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .marginAmount(marginAmount)
                .marginPercent(marginPercent)
                .build();
    }
}