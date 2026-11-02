package com.electronicstore.inventory.controller;

import com.electronicstore.inventory.dto.request.LoginRequest;
import com.electronicstore.inventory.dto.request.RefreshTokenRequest;
import com.electronicstore.inventory.dto.request.RegisterRequest;
import com.electronicstore.inventory.dto.response.AuthResponse;
import com.electronicstore.inventory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.electronicstore.inventory.dto.request.ForgotPasswordRequest;
import com.electronicstore.inventory.dto.request.LoginRequest;
import com.electronicstore.inventory.dto.request.RefreshTokenRequest;
import com.electronicstore.inventory.dto.request.RegisterRequest;
import com.electronicstore.inventory.dto.request.ResetPasswordRequest;
import com.electronicstore.inventory.dto.response.AuthResponse;
import com.electronicstore.inventory.dto.response.ForgotPasswordResponse;

/**
 * FR1 (Registration) and FR2 (Authentication & Session Management).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT: client discards tokens. Token blacklist (Appendix D) can be
        // added via a persisted denylist + a check in JwtAuthenticationFilter if needed.
        return ResponseEntity.noContent().build();
    }
}
