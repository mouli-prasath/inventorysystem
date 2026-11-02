package com.electronicstore.inventory.dto.request;

import com.electronicstore.inventory.enums.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Store location is required")
    private String storeLocation;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    private Integer quantity; // positive number; direction is derived from movementType

    private String reference;

    private String notes;
    private Boolean managerOverride;
}
