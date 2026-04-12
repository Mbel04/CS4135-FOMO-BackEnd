package com.fomo.backend.service;

import com.fomo.backend.config.SupabaseStorageProperties;
import com.fomo.backend.dto.response.PostResponse;
import com.fomo.backend.entity.*;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final SavedPostRepository savedPostRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SupabaseStorageService storageService;

    @Transactional
    public PostResponse createPost(String email, String content, List<UUID> categoryIds,
                                   List<UUID> taggedUserIds, MultipartFile media) {
        if ((content == null || content.isBlank()) && (media == null || media.isEmpty())) {
            throw new BadRequestException("A post must have text content or a media file");
        }

        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Category> categories = new HashSet<>();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
        }

        Set<User> taggedUsers = new HashSet<>();
        if (taggedUserIds != null && !taggedUserIds.isEmpty()) {
            taggedUsers = new HashSet<>(userRepository.findAllById(taggedUserIds));
        }

        Post.PostBuilder builder = Post.builder()
                .author(author)
                .content(content != null ? content : "")
                .categories(categories)
                .taggedUsers(taggedUsers);

        if (media != null && !media.isEmpty()) {
            String storagePath = storageService.uploadFile(
                    SupabaseStorageProperties.POSTS_BUCKET, author.getId().toString(), media);
            builder.mediaStoragePath(storagePath)
                    .mediaUrl(storageService.getPublicUrl(SupabaseStorageProperties.POSTS_BUCKET, storagePath))
                    .mediaType(storageService.detectMediaType(media));
        }

        Post post = postRepository.save(builder.build());

        for (User tagged : taggedUsers) {
            notificationService.createNotification(tagged, Notification.NotificationType.TAG,
                    author.getUsername() + " tagged you in a post", post.getId());
        }

        return PostResponse.from(post, 0);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getFeed() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(post -> PostResponse.from(post, likeRepository.countByPost(post)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return PostResponse.from(post, likeRepository.countByPost(post));
    }

    @Transactional
    public void deletePost(String email, UUID postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean isOwner = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You are not allowed to delete this post");
        }

        if (post.getMediaStoragePath() != null) {
            storageService.deleteFile(SupabaseStorageProperties.POSTS_BUCKET, post.getMediaStoragePath());
        }

        postRepository.delete(post);
    }

    @Transactional
    public void likePost(String email, UUID postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (likeRepository.existsByUserAndPost(user, post)) {
            throw new BadRequestException("Post is already liked");
        }
        Like like = Like.builder().user(user).post(post).build();
        likeRepository.save(like);

        if (!post.getAuthor().getId().equals(user.getId())) {
            notificationService.createNotification(post.getAuthor(), Notification.NotificationType.LIKE,
                    user.getUsername() + " liked your post", post.getId());
        }
    }

    @Transactional
    public void unlikePost(String email, UUID postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BadRequestException("Post is not liked"));
        likeRepository.delete(like);
    }

    @Transactional
    public void savePost(String email, UUID postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (savedPostRepository.existsByUserAndPost(user, post)) {
            throw new BadRequestException("Post is already saved");
        }
        savedPostRepository.save(SavedPost.builder().user(user).post(post).build());
    }

    @Transactional
    public void unsavePost(String email, UUID postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        SavedPost savedPost = savedPostRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BadRequestException("Post is not saved"));
        savedPostRepository.delete(savedPost);
    }
}
