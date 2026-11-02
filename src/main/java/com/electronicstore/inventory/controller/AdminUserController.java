package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.ChangeRoleRequest;
import com.electronicstore.inventory.dto.response.UserResponse;
import com.electronicstore.inventory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FR14: user account lifecycle management. Read access is Store Manager (limited) + Admin per
 * Appendix A's "Manage Users" row; role changes and deactivation are Admin-only (enforced in
 * SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.reactivate(id));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlock(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unlockAccount(id));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(userService.changeRole(id, request));
    }
}
