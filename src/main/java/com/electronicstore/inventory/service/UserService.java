package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.ChangeRoleRequest;
import com.electronicstore.inventory.dto.request.UpdateProfileRequest;
import com.electronicstore.inventory.dto.response.UserResponse;
import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR14: Comprehensive System Administration and Configuration Management (user account
 * lifecycle portion). Appendix H: GET/PUT /users/profile (self-service) plus the admin-facing
 * list/view/deactivate/reactivate/role-change operations.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuditService auditService;
    public UserResponse getProfile(Long userId) {
        return toResponse(findEntity(userId));
    }
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findEntity(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStoreLocation(request.getStoreLocation());
        return toResponse(userRepository.save(user));
    }
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }
    public UserResponse getById(Long id) {
        return toResponse(findEntity(id));
    }
    @Transactional
    public UserResponse deactivate(Long id) {
        User user = findEntity(id);
        user.setActive(false);
        User saved = userRepository.save(user);
        auditService.logCurrentUser("USER_DEACTIVATED", "USER", String.valueOf(id), "active", "inactive");
        return toResponse(saved);
    }

    @Transactional
    public UserResponse reactivate(Long id) {
        User user = findEntity(id);
        user.setActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        User saved = userRepository.save(user);
        auditService.logCurrentUser("USER_REACTIVATED", "USER", String.valueOf(id), "inactive", "active");
        return toResponse(saved);
    }
    @Transactional
    public UserResponse unlockAccount(Long id) {
        User user = findEntity(id);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        User saved = userRepository.save(user);
        auditService.logCurrentUser("ACCOUNT_UNLOCKED", "USER", String.valueOf(id), "locked", "unlocked");
        return toResponse(saved);
    }
    @Transactional
    public UserResponse changeRole(Long id, ChangeRoleRequest request) {
        User user = findEntity(id);
        String oldRole = user.getRole().name();
        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        auditService.logCurrentUser("ROLE_CHANGED", "USER", String.valueOf(id), oldRole, request.getRole().name());
        return toResponse(saved);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .role(u.getRole())
                .employeeId(u.getEmployeeId())
                .storeLocation(u.getStoreLocation())
                .isActive(u.isActive())
                .build();
    }
}
