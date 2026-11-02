package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.response.AuditLogResponse;
import com.electronicstore.inventory.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Appendix H: GET /admin/audit-logs. Restricted to Store Manager (limited) / Admin per the
 * RBAC matrix (Appendix A: "Audit Logs" row).
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditService auditService;
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAll(
            @PageableDefault(size = 50, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getAll(pageable));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getByUser(
            @PathVariable Long userId, @PageableDefault(size = 50, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getByUser(userId, pageable));
    }
    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<Page<AuditLogResponse>> getByEntityType(
            @PathVariable String entityType, @PageableDefault(size = 50, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getByEntityType(entityType, pageable));
    }
    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getByAction(
            @PathVariable String action, @PageableDefault(size = 50, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getByAction(action, pageable));
    }
    @GetMapping("/range")
    public ResponseEntity<Page<AuditLogResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 50, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getByDateRange(start, end, pageable));
    }
}
