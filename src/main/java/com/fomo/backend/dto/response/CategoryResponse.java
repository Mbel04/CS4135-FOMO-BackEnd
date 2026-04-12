package com.fomo.backend.dto.response;

import com.fomo.backend.entity.Category;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponse {
    private UUID id;
    private String name;

    public static CategoryResponse from(Category category) {
        CategoryResponse r = new CategoryResponse();
        r.setId(category.getId());
        r.setName(category.getName());
        return r;
    }
}
