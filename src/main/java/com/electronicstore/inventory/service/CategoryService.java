package com.electronicstore.inventory.service;

import com.electronicstore.inventory.dto.request.CategoryRequest;
import com.electronicstore.inventory.dto.response.CategoryResponse;
import com.electronicstore.inventory.entity.Category;
import com.electronicstore.inventory.exception.DuplicateResourceException;
import com.electronicstore.inventory.exception.ResourceNotFoundException;
import com.electronicstore.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException("A category with this name already exists.");
        }
        Category parent = resolveParent(request.getParentCategoryId());
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .parentCategory(parent)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder())
                .isActive(true)
                .build();
        return toResponse(categoryRepository.save(category));
    }
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        if (!category.getCategoryName().equalsIgnoreCase(request.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException("A category with this name already exists.");
        }
        category.setCategoryName(request.getCategoryName());
        category.setParentCategory(resolveParent(request.getParentCategoryId()));
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<CategoryResponse> getTopLevel() {
        return categoryRepository.findByParentCategoryIsNull().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Category category = findEntity(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) return null;
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + parentId));
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .categoryName(c.getCategoryName())
                .parentCategoryId(c.getParentCategory() != null ? c.getParentCategory().getId() : null)
                .parentCategoryName(c.getParentCategory() != null ? c.getParentCategory().getCategoryName() : null)
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .isActive(c.isActive())
                .displayOrder(c.getDisplayOrder())
                .build();
    }
}
