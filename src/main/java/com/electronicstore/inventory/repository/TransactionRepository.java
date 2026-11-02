package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    Page<Transaction> findByCustomerId(Long customerId, Pageable pageable);

    Page<Transaction> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * FR10: aggregate sales figures for a period, excluding voided transactions.
     * Returns exactly one row: [totalRevenue, totalTax, totalDiscount, transactionCount].
     * Declared as List<Object[]> (not a bare Object[]) because that's what Hibernate
     * actually returns for a multi-column JPQL projection - a repository method
     * declared to return Object[] directly does NOT auto-unwrap that single-row
     * list, it just converts the *list* into an array, so row[0] ends up being
     * the real row (itself an Object[]) instead of the first column. Callers
     * must take .get(0) to get the actual row.
     */
    @Query("select sum(t.totalAmount), sum(t.taxAmount), " +
            "sum(t.discountAmount), count(t) " +
            "from Transaction t where t.transactionDate between :start and :end " +
            "and t.status <> com.electronicstore.inventory.enums.TransactionStatus.CANCELLED")
    List<Object[]> getSalesAggregate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}