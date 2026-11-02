package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    List<Product> findByStockQuantityLessThanEqualAndIsActiveTrue(Integer threshold);

    @Query("select p from Product p where p.stockQuantity <= p.reorderPoint and p.isActive = true")
    List<Product> findProductsAtOrBelowReorderPoint();

    /**
     * FR10/FR11: overall inventory valuation snapshot.
     * Returns exactly one row: [totalCostValue, totalRetailValue, totalUnits, activeProductCount].
     * Declared as List<Object[]> for the same reason as TransactionRepository's
     * getSalesAggregate() - see that method's Javadoc. Money sums are left
     * nullable (no JPQL coalesce against a bare double literal) and handled in
     * Java instead.
     */
    @Query("select sum(p.stockQuantity * p.costPrice), " +
            "sum(p.stockQuantity * p.price), " +
            "coalesce(sum(p.stockQuantity), 0), count(p) " +
            "from Product p where p.isActive = true")
    List<Object[]> getInventoryValuationAggregate();

    /**
     * FR10/FR11: inventory valuation broken down per category.
     * Each row: [categoryName, costValue, retailValue, totalUnits].
     */
    @Query("select coalesce(p.category.categoryName, 'Uncategorized'), " +
            "sum(p.stockQuantity * p.costPrice), " +
            "sum(p.stockQuantity * p.price), " +
            "coalesce(sum(p.stockQuantity), 0) " +
            "from Product p where p.isActive = true " +
            "group by p.category.categoryName")
    List<Object[]> getInventoryValuationByCategory();
}