package com.electronicstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * General-purpose audit trail (Appendix B: AuditLogs table), distinct from
 * {@link StockMovement} which covers inventory-specific movements only. This table captures
 * security-relevant and business-sensitive actions across the system: logins, registrations,
 * price changes, role/account changes, PO approvals, and so on.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 100)
    private String action;
    @Column(name = "entity_type", length = 50)
    private String entityType;
    @Column(name = "entity_id", length = 50)
    private String entityId;
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    @Column(length = 45)
    private String ipAddress;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
