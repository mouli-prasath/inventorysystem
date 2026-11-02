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
public class TopSellingProductResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Long quantitySold;
    private BigDecimal revenue;
}
