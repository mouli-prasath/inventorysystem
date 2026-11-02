package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.SupplierRequest;
import com.electronicstore.inventory.dto.response.SupplierResponse;
import com.electronicstore.inventory.entity.Supplier;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Part of FR8: Supplier Management and Purchase Order System (vendor profile portion).
 */
@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .supplierName(request.getSupplierName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .paymentTerms(request.getPaymentTerms())
                .isActive(true)
                .build();
        return toResponse(supplierRepository.save(supplier));
    }
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findEntity(id);
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setPaymentTerms(request.getPaymentTerms());
        return toResponse(supplierRepository.save(supplier));
    }
    public SupplierResponse getById(Long id) {
        return toResponse(findEntity(id));
    }
    public List<SupplierResponse> getAll(boolean activeOnly) {
        List<Supplier> suppliers = activeOnly ? supplierRepository.findByIsActiveTrue() : supplierRepository.findAll();
        return suppliers.stream().map(this::toResponse).toList();
    }

    public List<SupplierResponse> search(String name) {
        return supplierRepository.findBySupplierNameContainingIgnoreCase(name).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Supplier supplier = findEntity(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
    private Supplier findEntity(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }
    private SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .supplierName(s.getSupplierName())
                .contactPerson(s.getContactPerson())
                .email(s.getEmail())
                .phone(s.getPhone())
                .address(s.getAddress())
                .paymentTerms(s.getPaymentTerms())
                .isActive(s.isActive())
                .build();
    }
}
