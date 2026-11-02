package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findByTransactionId(Long transactionId);
}
