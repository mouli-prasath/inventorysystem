package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.response.AuditLogResponse;
import com.electronicstore.inventory.entity.AuditLog;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Appendix B: AuditLogs table. General-purpose action trail, distinct from the
 * inventory-specific {@link StockMovement} audit handled by InventoryService/TransactionService.
 *
 * Called by other services at the point of the sensitive action (login, registration, price
 * change, approval, account status change, etc.) rather than via a blanket interceptor, so each
 * call site can supply a meaningful old/new value pair instead of a generic method-name dump.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Runs in its own transaction (REQUIRES_NEW) so an audit entry survives even when the
     * caller's transaction subsequently rolls back - e.g. a failed login attempt still needs
     * to be recorded even though no other state change should be persisted.
     */
    @Transactional
    public void log(User user, String action, String entityType, String entityId,
                    String oldValue, String newValue) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(entry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIndependent(User user, String action, String entityType, String entityId,
                               String oldValue, String newValue) {
        log(user, action, entityType, entityId, oldValue, newValue);
    }
    /** Convenience overload that resolves the current authenticated user automatically. */
    @Transactional
    public void logCurrentUser(String action, String entityType, String entityId,
                                String oldValue, String newValue) {
        log(currentUserOrNull(), action, entityType, entityId, oldValue, newValue);
    }

    public Page<AuditLogResponse> getByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    private User currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : "system/unknown")
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}
