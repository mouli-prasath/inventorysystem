package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoyaltyAdjustmentRequest {

    /** Positive to add points, negative to deduct (e.g. redemption). */
    @NotNull(message = "Points delta is required")
    private Integer pointsDelta;

    private String reason;
}
