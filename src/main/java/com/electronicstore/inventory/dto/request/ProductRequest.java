package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "SKU must be 6-20 alphanumeric characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String brand;

    private String model;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.0", message = "Cost price must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    private Long supplierId;

    @Pattern(regexp = "^$|^[0-9]{8,13}$", message = "Invalid barcode format")
    private String barcode;

    private BigDecimal weight;

    private String dimensions;

    private Integer warrantyPeriod;

    /** FR4 image management: validated as a well-formed http(s) URL when provided. Empty string
     *  (not just omitted) is accepted as "no image yet", matching this codebase's convention
     *  for other optional-but-validated fields (see barcode above). */
    @Pattern(regexp = "^$|^https?://.+", message = "Image URL must be a valid http(s) URL")
    private String imageUrl;

    @Pattern(regexp = "^$|^https?://.+", message = "Spec sheet URL must be a valid http(s) URL")
    private String specSheetUrl;
}
