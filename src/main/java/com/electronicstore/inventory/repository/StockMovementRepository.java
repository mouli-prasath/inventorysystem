package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductIdOrderByTimestampDesc(Long productId, Pageable pageable);
}
