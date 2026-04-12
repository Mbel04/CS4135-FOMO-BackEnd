package com.fomo.backend.controller;

import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search users and posts")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/users")
    @Operation(summary = "Search for users by username")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.searchUsers(query));
    }

    @GetMapping("/posts")
    @Operation(summary = "Search for posts by category name")
    public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam("q") String categoryName) {
        return ResponseEntity.ok(searchService.searchPostsByCategory(categoryName));
    }
}
