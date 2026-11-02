package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.SendNotificationRequest;
import com.electronicstore.inventory.dto.response.NotificationResponse;
import com.electronicstore.inventory.entity.Notification;
import com.electronicstore.inventory.entity.Product;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.enums.NotificationPriority;
import com.electronicstore.inventory.enums.NotificationType;
import com.electronicstore.inventory.enums.PurchaseOrderStatus;
import com.electronicstore.inventory.enums.Role;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.NotificationRepository;
import com.electronicstore.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FR12: Comprehensive Notification and Communication System.
 *
 * This service is called two ways:
 *  1) Directly by users/admins via NotificationController (manual/broadcast messages, Appendix H:
 *     POST /admin/notifications).
 *  2) Internally by other services (ProductService, InventoryService, PurchaseOrderService, etc.)
 *     via the small "notifyXyz(...)" helper methods below, so business events automatically raise
 *     the right alert without those services needing to know about roles or delivery details.
 *
 * Delivery is currently in-app (persisted rows the client polls/reads). Email/SMS/push fan-out
 * (Appendix E-style multi-channel delivery) can be added as a listener on notification creation
 * without changing this service's public surface.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final List<Role> INVENTORY_ALERT_ROLES = List.of(
            Role.INVENTORY_MANAGER, Role.STORE_MANAGER, Role.ADMIN);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    public Page<NotificationResponse> getForUser(Long userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedDateDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable);
        return page.map(this::toResponse);
    }
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification not found for this user: " + notificationId);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // ---- manual send / broadcast (Appendix H: POST /admin/notifications) ----

    @Transactional
    public int send(SendNotificationRequest request) {
        List<User> recipients;

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            recipients = userRepository.findAllById(request.getUserIds());
        } else if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            recipients = userRepository.findByRoleInAndIsActiveTrue(request.getRoles());
        } else {
            recipients = userRepository.findByRoleInAndIsActiveTrue(List.of(Role.values()));
        }

        NotificationPriority priority = request.getPriority() == null ? NotificationPriority.NORMAL : request.getPriority();

        for (User recipient : recipients) {
            persist(recipient, request.getTitle(), request.getMessage(), request.getType(), priority, null);
        }
        return recipients.size();
    }

    // ---- internal event hooks (called by other services) ----

    /** FR5/FR12: a product has dropped to or below its reorder point. */
    @Transactional
    public void notifyLowStock(Product product) {
        boolean outOfStock = product.getStockQuantity() != null && product.getStockQuantity() <= 0;
        String title = outOfStock ? "Out of stock" : "Low stock alert";
        String message = outOfStock
                ? "Product " + product.getSku() + " (" + product.getName() + ") is out of stock."
                : "Product " + product.getSku() + " (" + product.getName() + ") is at " + product.getStockQuantity()
                        + " units, at or below its reorder point of " + product.getReorderPoint() + ".";

        broadcastToRoles(INVENTORY_ALERT_ROLES, title, message,
                outOfStock ? NotificationType.OUT_OF_STOCK : NotificationType.LOW_STOCK,
                outOfStock ? NotificationPriority.CRITICAL : NotificationPriority.HIGH,
                "PRODUCT:" + product.getId());
    }

    /** FR8/FR9/FR12: a purchase order's status changed. */
    @Transactional
    public void notifyPurchaseOrderStatusChange(String poNumber, PurchaseOrderStatus newStatus, User createdBy) {
        String message = "Purchase order " + poNumber + " is now " + newStatus.name().replace('_', ' ') + ".";

        if (createdBy != null) {
            persist(createdBy, "Purchase order update", message,
                    NotificationType.PURCHASE_ORDER_STATUS, NotificationPriority.NORMAL, "PO:" + poNumber);
        }
        if (newStatus == PurchaseOrderStatus.RECEIVED || newStatus == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            broadcastToRoles(INVENTORY_ALERT_ROLES, "Delivery received", message,
                    NotificationType.DELIVERY_RECEIVED, NotificationPriority.NORMAL, "PO:" + poNumber);
        }
    }

    /** FR6/FR12: a return was processed. */
    @Transactional
    public void notifyReturnProcessed(String transactionNumber, String productSku, int quantity) {
        String message = "Return processed for " + quantity + " unit(s) of " + productSku
                + " against transaction " + transactionNumber + ".";
        broadcastToRoles(List.of(Role.STORE_MANAGER, Role.ADMIN), "Return processed", message,
                NotificationType.RETURN_PROCESSED, NotificationPriority.LOW, "TXN:" + transactionNumber);
    }

    // ---- internal helpers ----

    private void broadcastToRoles(List<Role> roles, String title, String message, NotificationType type,
                                   NotificationPriority priority, String reference) {
        List<User> recipients = userRepository.findByRoleInAndIsActiveTrue(roles);
        for (User recipient : recipients) {
            persist(recipient, title, message, type, priority, reference);
        }
    }

    private void persist(User user, String title, String message, NotificationType type,
                          NotificationPriority priority, String reference) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .reference(reference)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .priority(n.getPriority())
                .isRead(n.isRead())
                .reference(n.getReference())
                .createdDate(n.getCreatedDate())
                .build();
    }
}
