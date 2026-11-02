package com.electronicstore.inventory.repository;

import com.electronicstore.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    List<Category> findByIsActiveTrue();
    List<Category> findByParentCategoryId(Long parentCategoryId);
    List<Category> findByParentCategoryIsNull();
}
