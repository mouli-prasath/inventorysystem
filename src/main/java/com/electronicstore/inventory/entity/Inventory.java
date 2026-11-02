package com.electronicstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "store_location"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "store_location", nullable = false, length = 100)
    private String storeLocation;
    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;
    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}
