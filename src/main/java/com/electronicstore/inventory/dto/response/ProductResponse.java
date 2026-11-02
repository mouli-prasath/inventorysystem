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
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String model;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private Integer reorderPoint;
    private Long supplierId;
    private String supplierName;
    private String barcode;
    private BigDecimal weight;
    private String dimensions;
    private Integer warrantyPeriod;
    private String imageUrl;
    private String specSheetUrl;
    private boolean isActive;
    private boolean lowStock;
    private BigDecimal effectivePrice;
}
