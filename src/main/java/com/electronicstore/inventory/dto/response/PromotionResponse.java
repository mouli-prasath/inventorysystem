package com.electronicstore.inventory.dto.response;

import com.electronicstore.inventory.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {
    private Long id;
    private String promotionName;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Long productScopeId;
    private String productScopeName;
    private Long categoryScopeId;
    private String categoryScopeName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private boolean currentlyRunning;
    private String description;
}
