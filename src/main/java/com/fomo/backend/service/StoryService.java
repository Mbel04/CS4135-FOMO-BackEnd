package com.fomo.backend.service;

import com.fomo.backend.config.SupabaseStorageProperties;
import com.fomo.backend.dto.response.StoryResponse;
import com.fomo.backend.entity.Story;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.FriendshipRepository;
import com.fomo.backend.repository.StoryRepository;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final SupabaseStorageService storageService;

    @Transactional
    public StoryResponse createStory(String email, String content, MultipartFile media) {
        if ((content == null || content.isBlank()) && (media == null || media.isEmpty())) {
            throw new BadRequestException("A story must have text content or a media file");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Story.StoryBuilder builder = Story.builder()
                .user(user)
                .content(content != null ? content : "");

        if (media != null && !media.isEmpty()) {
            String storagePath = storageService.uploadFile(
                    SupabaseStorageProperties.STORIES_BUCKET, user.getId().toString(), media);
            builder.mediaStoragePath(storagePath)
                    .mediaUrl(storageService.getPublicUrl(SupabaseStorageProperties.STORIES_BUCKET, storagePath))
                    .mediaType(storageService.detectMediaType(media));
        }

        return StoryResponse.from(storyRepository.save(builder.build()));
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> getFriendsStories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<User> friends = friendshipRepository.findAllByUser(user).stream()
                .map(f -> f.getUser1().getId().equals(user.getId()) ? f.getUser2() : f.getUser1())
                .collect(Collectors.toList());
        // Include current user so their own stories appear (previously only friends were queried).
        List<User> sources = new ArrayList<>(friends.size() + 1);
        sources.add(user);
        sources.addAll(friends);
        return storyRepository.findActiveStoriesForUsers(sources, LocalDateTime.now()).stream()
                .map(StoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStory(String email, UUID storyId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found"));

        boolean isOwner = story.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You are not allowed to delete this story");
        }

        if (story.getMediaStoragePath() != null) {
            storageService.deleteFile(SupabaseStorageProperties.STORIES_BUCKET, story.getMediaStoragePath());
        }

        storyRepository.delete(story);
    }

    @Transactional
    public void deleteExpiredStories() {
        List<Story> expired = storyRepository.findExpiredStories(LocalDateTime.now());
        for (Story story : expired) {
            if (story.getMediaStoragePath() != null) {
                storageService.deleteFile(SupabaseStorageProperties.STORIES_BUCKET, story.getMediaStoragePath());
            }
        }
        storyRepository.deleteAll(expired);
    }
}
