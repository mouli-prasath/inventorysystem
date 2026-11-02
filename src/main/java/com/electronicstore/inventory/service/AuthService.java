package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.ChangePasswordRequest;
import com.electronicstore.inventory.dto.request.ForgotPasswordRequest;
import com.electronicstore.inventory.dto.request.LoginRequest;
import com.electronicstore.inventory.dto.request.RefreshTokenRequest;
import com.electronicstore.inventory.dto.request.RegisterRequest;
import com.electronicstore.inventory.dto.request.ResetPasswordRequest;
import com.electronicstore.inventory.dto.response.AuthResponse;
import com.electronicstore.inventory.dto.response.ForgotPasswordResponse;
import com.electronicstore.inventory.dto.response.UserResponse;
import com.electronicstore.inventory.entity.PasswordResetToken;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.enums.Role;
import com.electronicstore.inventory.exception.DuplicateResourceException;
import com.electronicstore.inventory.exception.InvalidCredentialsException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.PasswordResetTokenRepository;
import com.electronicstore.inventory.repository.UserRepository;
import com.electronicstore.inventory.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists.");
        }
        if (request.getRole() != Role.CUSTOMER
                && (request.getEmployeeId() == null || request.getEmployeeId().isBlank())) {
            throw new IllegalArgumentException("Employee ID is required for staff accounts.");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .storeLocation(request.getStoreLocation())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);

        auditService.log(saved, "REGISTER", "USER", String.valueOf(saved.getId()),
                null, "role=" + saved.getRole() + ", username=" + saved.getUsername());

        String accessToken = jwtService.generateAccessToken(saved);
        String refreshToken = jwtService.generateRefreshToken(saved);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(saved))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmailOrEmployeeId(
                        request.getIdentifier(), request.getIdentifier(), request.getIdentifier())
                .orElse(null);
        if (user != null && user.isAccountLocked()) {
            auditService.logIndependent(user, "LOGIN_BLOCKED_LOCKED", "USER",
                    String.valueOf(user.getId()), null, null);
            throw new InvalidCredentialsException(
                    "This account is locked after repeated failed login attempts. Contact an administrator to unlock it.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
        } catch (Exception ex) {
            if (user != null) {
                loginAttemptService.recordFailedAttempt(user.getId());
            }
            auditService.logIndependent(user, "LOGIN_FAILED", "USER", request.getIdentifier(), null, null);
            throw new InvalidCredentialsException("Invalid credentials provided.");
        }
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        auditService.log(user, "LOGIN_SUCCESS", "USER", String.valueOf(user.getId()), null, null);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        UserDetails userDetails = user;
        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(user, "PASSWORD_CHANGED", "USER", String.valueOf(user.getId()), null, null);
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String genericMessage = "If an account matches that identifier, a password reset token has been issued.";

        User user = userRepository.findByUsernameOrEmailOrEmployeeId(
                        request.getIdentifier(), request.getIdentifier(), request.getIdentifier())
                .orElse(null);

        if (user == null) {
            return ForgotPasswordResponse.builder().message(genericMessage).build();
        }

        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        auditService.log(user, "PASSWORD_RESET_REQUESTED", "USER", String.valueOf(user.getId()), null, null);

        return ForgotPasswordResponse.builder()
                .message(genericMessage)
                .devOnlyResetToken(token)
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token."));

        if (!resetToken.isValid()) {
            throw new InvalidCredentialsException("Invalid or expired reset token.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditService.log(user, "PASSWORD_RESET_COMPLETED", "USER", String.valueOf(user.getId()), null, null);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .employeeId(user.getEmployeeId())
                .storeLocation(user.getStoreLocation())
                .isActive(user.isActive())
                .build();
    }
}