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
public class ProfitMarginResponse {
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal marginAmount;
    private BigDecimal marginPercent;
}
