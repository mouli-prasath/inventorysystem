package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.SendNotificationRequest;
import com.electronicstore.inventory.dto.response.NotificationResponse;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FR12: Comprehensive Notification and Communication System.
 * Personal endpoints operate on the authenticated user; /admin endpoint (Appendix H:
 * POST /admin/notifications) lets Admin/Store Manager broadcast to specific users or roles.
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping("/api/notifications")
    public ResponseEntity<Page<NotificationResponse>> getMine(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable) {
        return ResponseEntity.ok(notificationService.getForUser(user.getId(), unreadOnly, pageable));
    }
    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(user.getId())));
    }
    @PutMapping("/api/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/api/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/api/admin/notifications")
    public ResponseEntity<Map<String, Integer>> send(@Valid @RequestBody SendNotificationRequest request) {
        int sentTo = notificationService.send(request);
        return ResponseEntity.ok(Map.of("recipientCount", sentTo));
    }
}
