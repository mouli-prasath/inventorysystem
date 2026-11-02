package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private Long parentCategoryId;

    private String description;

    private String imageUrl;

    private Integer displayOrder;
}
