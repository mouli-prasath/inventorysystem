package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.User;
import com.electronicstore.inventory.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByUsernameOrEmailOrEmployeeId(String username, String email, String employeeId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    List<User> findByRoleInAndIsActiveTrue(List<Role> roles);
}
