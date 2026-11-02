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
public class CategoryValuationResponse {
    private String categoryName;
    private BigDecimal costValue;
    private BigDecimal retailValue;
    private Long totalUnits;
}
