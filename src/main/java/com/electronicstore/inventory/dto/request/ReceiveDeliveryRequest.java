package com.electronicstore.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReceiveDeliveryRequest {

    /** Which line items are being received, and how many units of each. */
    @NotEmpty(message = "At least one received line item is required")
    @Valid
    private List<ReceivedLineItem> receivedItems;

    @Data
    public static class ReceivedLineItem {
        private Long purchaseOrderItemId;
        private Integer quantityReceived;
    }
}
