package com.fomo.backend.controller;

import com.fomo.backend.dto.request.CreateCategoryRequest;
import com.fomo.backend.dto.response.CategoryResponse;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Post category management")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category (admin only)")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "Get posts by category")
    public ResponseEntity<List<PostResponse>> getPostsByCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getPostsByCategory(id));
    }
}
