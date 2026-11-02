package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.dto.response.TopSellingProductResponse;
import com.electronicstore.inventory.entity.TransactionItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransactionId(Long transactionId);

    /**
     * FR10/FR11: best sellers by quantity sold within a period, excluding voided transactions.
     * Line revenue = (unitPrice * quantity) - discountAmount, aggregated per product.
     */
    @Query(value = "select new com.electronicstore.inventory.dto.response.TopSellingProductResponse(" +
            "ti.product.id, ti.product.name, ti.product.sku, " +
            "sum(ti.quantity), " +
            "sum(ti.unitPrice * ti.quantity - coalesce(ti.discountAmount, 0.0))) " +
            "from TransactionItem ti " +
            "where ti.transaction.transactionDate between :start and :end " +
            "and ti.transaction.status <> com.electronicstore.inventory.enums.TransactionStatus.CANCELLED " +
            "group by ti.product.id, ti.product.name, ti.product.sku " +
            "order by sum(ti.quantity) desc",
            countQuery = "select count(distinct ti.product.id) from TransactionItem ti " +
                    "where ti.transaction.transactionDate between :start and :end " +
                    "and ti.transaction.status <> com.electronicstore.inventory.enums.TransactionStatus.CANCELLED")
    Page<TopSellingProductResponse> findTopSellingProducts(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
