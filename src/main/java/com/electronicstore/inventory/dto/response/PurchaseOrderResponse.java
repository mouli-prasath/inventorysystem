package com.electronicstore.inventory.dto.response;

import com.electronicstore.inventory.enums.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDate expectedDelivery;
    private PurchaseOrderStatus status;
    private String storeLocation;
    private BigDecimal totalAmount;
    private String createdByName;
    private String notes;
    private List<PurchaseOrderItemResponse> items;
}
