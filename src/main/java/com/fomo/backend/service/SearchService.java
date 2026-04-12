package com.fomo.backend.service;

import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.repository.LikeRepository;
import com.fomo.backend.repository.PostRepository;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> searchPostsByCategory(String categoryName) {
        return postRepository.findByCategoryName(categoryName).stream()
                .map(post -> PostResponse.from(post, likeRepository.countByPost(post)))
                .collect(Collectors.toList());
    }
}
