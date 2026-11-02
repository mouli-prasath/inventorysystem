package com.electronicstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String sku;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(length = 100)
    private String brand;
    @Column(length = 100)
    private String model;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;
    @Column(name = "reorder_point", nullable = false)
    @Builder.Default
    private Integer reorderPoint = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(unique = true, length = 13)
    private String barcode;

    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    @Column(length = 50)
    private String dimensions;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod;

    /** FR4: "Image management shall handle product photos, specification sheets, and
     *  marketing materials with validation." Primary product photo and spec sheet are stored
     *  as URLs (uploaded via whatever file/blob storage the deployment uses); a full photo
     *  gallery beyond the primary image would need a separate ProductImage table if/when
     *  multiple photos per product are required. */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "spec_sheet_url", length = 500)
    private String specSheetUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
