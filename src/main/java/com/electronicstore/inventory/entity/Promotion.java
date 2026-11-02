package com.electronicstore.inventory.entity;

import com.electronicstore.inventory.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FR4/scope: "Price management and promotional system". A promotion applies to either a
 * single product, an entire category, or store-wide (both productScope and categoryScope null).
 */
@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "promotion_name", nullable = false, length = 150)
    private String promotionName;
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_scope_id")
    private Product productScope;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_scope_id")
    private Category categoryScope;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
    @Column(columnDefinition = "TEXT")
    private String description;

    public boolean isCurrentlyValid(LocalDateTime at) {
        return isActive && !at.isBefore(startDate) && !at.isAfter(endDate);
    }

    public boolean appliesToProduct(Product product) {
        if (productScope != null) {
            return productScope.getId().equals(product.getId());
        }
        if (categoryScope != null) {
            return product.getCategory() != null && categoryScope.getId().equals(product.getCategory().getId());
        }
        return true; // store-wide
    }

    public BigDecimal applyTo(BigDecimal price) {
        BigDecimal discounted = discountType == DiscountType.PERCENTAGE
                ? price.subtract(price.multiply(discountValue)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP))
                : price.subtract(discountValue);
        return discounted.max(BigDecimal.ZERO);
    }
}
