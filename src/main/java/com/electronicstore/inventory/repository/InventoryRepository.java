package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductIdAndStoreLocation(Long productId, String storeLocation);
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByStoreLocation(String storeLocation);
}
