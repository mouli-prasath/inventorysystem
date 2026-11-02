package com.electronicstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;
}
