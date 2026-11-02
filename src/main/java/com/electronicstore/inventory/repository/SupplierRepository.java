package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByIsActiveTrue();
    List<Supplier> findBySupplierNameContainingIgnoreCase(String name);
}
