package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * FR5/Appendix H: POST /inventory/count. Unlike /inventory/adjustment (a signed delta),
 * a cycle count reports the physically counted quantity - the system computes the
 * correction itself and records it as an ADJUSTMENT movement.
 */
@Data
public class CycleCountRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Store location is required")
    private String storeLocation;

    @NotNull(message = "Counted quantity is required")
    @Min(value = 0, message = "Counted quantity cannot be negative")
    private Integer countedQuantity;

    private String notes;
}
