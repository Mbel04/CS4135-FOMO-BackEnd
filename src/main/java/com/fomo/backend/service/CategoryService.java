package com.fomo.backend.service;

import com.fomo.backend.dto.request.CreateCategoryRequest;
import com.fomo.backend.dto.response.CategoryResponse;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.entity.Category;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.CategoryRepository;
import com.fomo.backend.repository.LikeRepository;
import com.fomo.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category already exists");
        }
        Category category = Category.builder().name(request.getName()).build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return postRepository.findByCategoriesContaining(category).stream()
                .map(post -> PostResponse.from(post, likeRepository.countByPost(post)))
                .collect(Collectors.toList());
    }
}
