package com.electronicstore.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    private LocalDate expectedDelivery;

    private String storeLocation;

    @NotEmpty(message = "Purchase order must include at least one item")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    private String notes;
}
