package com.electronicstore.inventory.security;

import com.electronicstore.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmailOrEmployeeId) throws UsernameNotFoundException {
        return userRepository
                .findByUsernameOrEmailOrEmployeeId(usernameOrEmailOrEmployeeId, usernameOrEmailOrEmployeeId, usernameOrEmailOrEmployeeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmailOrEmployeeId));
    }
}
