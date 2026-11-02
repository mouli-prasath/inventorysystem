package com.electronicstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValuationResponse {
    private BigDecimal totalCostValue;
    private BigDecimal totalRetailValue;
    private BigDecimal potentialProfit;
    private Long totalUnits;
    private Long activeProductCount;
    private List<CategoryValuationResponse> byCategory;
}
