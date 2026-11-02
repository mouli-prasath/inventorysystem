package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.CustomerRequest;
import com.electronicstore.inventory.dto.response.CustomerResponse;
import com.electronicstore.inventory.entity.Customer;
import com.electronicstore.inventory.exception.DuplicateResourceException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Part of FR7: Customer Relationship Management System (profile portion).
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A customer with this email already exists.");
        }
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(blankToNull(request.getEmail()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .isActive(true)
                .build();

        return toResponse(customerRepository.save(customer));
    }
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntity(id);
        String newEmail = blankToNull(request.getEmail());
        if (newEmail != null && !newEmail.equalsIgnoreCase(customer.getEmail())
                && customerRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("A customer with this email already exists.");
        }
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(newEmail);
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setPostalCode(request.getPostalCode());

        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public Page<CustomerResponse> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return customerRepository.findAll(pageable).map(this::toResponse);
        }
        return customerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = findEntity(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }
    Customer findEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
    @Transactional
    public CustomerResponse adjustLoyaltyPoints(Long id, int delta) {
        Customer customer = findEntity(id);
        int newBalance = customer.getLoyaltyPoints() + delta;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Adjustment would result in a negative loyalty point balance.");
        }
        customer.setLoyaltyPoints(newBalance);
        return toResponse(customerRepository.save(customer));
    }
    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .city(c.getCity())
                .postalCode(c.getPostalCode())
                .loyaltyPoints(c.getLoyaltyPoints())
                .totalPurchases(c.getTotalPurchases())
                .registrationDate(c.getRegistrationDate())
                .lastPurchaseDate(c.getLastPurchaseDate())
                .isActive(c.isActive())
                .build();
    }
}
