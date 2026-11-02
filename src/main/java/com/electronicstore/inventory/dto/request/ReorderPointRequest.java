package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReorderPointRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;
}
