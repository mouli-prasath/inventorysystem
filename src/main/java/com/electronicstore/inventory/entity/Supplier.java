package com.electronicstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    @Column(length = 100)
    private String email;
    @Column(length = 15)
    private String phone;
    @Column(columnDefinition = "TEXT")
    private String address;
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;
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
