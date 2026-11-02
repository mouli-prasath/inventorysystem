package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.repository.ProductRepository;
import com.electronicstore.inventory.repository.TransactionRepository;
import com.electronicstore.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Appendix H: GET /admin/system-health. A lightweight, dependency-free health check - confirms
 * the database is reachable via a trivial count query and reports basic runtime stats. For
 * deeper infrastructure monitoring (disk, thread pools, custom health indicators), Spring Boot
 * Actuator can be layered in later without conflicting with this endpoint's path.
 */
@RestController
@RequiredArgsConstructor
public class SystemHealthController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;

    private static final LocalDateTime STARTED_AT = LocalDateTime.now();

    @GetMapping("/api/admin/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("uptimeSince", STARTED_AT);
        health.put("timezone", ZoneId.systemDefault().toString());

        Map<String, Object> database = new LinkedHashMap<>();
        try {
            long userCount = userRepository.count();
            database.put("status", "UP");
            database.put("userCount", userCount);
            database.put("productCount", productRepository.count());
            database.put("transactionCount", transactionRepository.count());
        } catch (Exception ex) {
            database.put("status", "DOWN");
            database.put("error", "Database connectivity check failed.");
            health.put("status", "DEGRADED");
        }
        health.put("database", database);

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("maxMB", runtime.maxMemory() / (1024 * 1024));
        memory.put("totalMB", runtime.totalMemory() / (1024 * 1024));
        memory.put("freeMB", runtime.freeMemory() / (1024 * 1024));
        health.put("memory", memory);

        return ResponseEntity.ok(health);
    }
}
