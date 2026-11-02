package com.electronicstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String categoryName;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private String imageUrl;
    private boolean isActive;
    private int displayOrder;
}
