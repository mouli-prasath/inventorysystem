package com.electronicstore.inventory.dto.request;

import com.electronicstore.inventory.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {

    @NotBlank(message = "Promotion name is required")
    private String promotionName;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be positive")
    private BigDecimal discountValue;

    /** Optional: applies to a single product. Leave null for category-wide or store-wide. */
    private Long productScopeId;

    /** Optional: applies to an entire category. Leave null (with productScopeId) for store-wide. */
    private Long categoryScopeId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private String description;
}
