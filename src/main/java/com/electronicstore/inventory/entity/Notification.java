package com.electronicstore.inventory.entity;

import com.electronicstore.inventory.enums.NotificationPriority;
import com.electronicstore.inventory.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FR12: Comprehensive Notification and Communication System.
 * A single delivery-agnostic notification record; multi-channel delivery (email/SMS/push)
 * can hang off this via an outbound dispatcher without changing the schema.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(length = 150)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;
    @Column(name = "reference", length = 100)
    private String reference;
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
