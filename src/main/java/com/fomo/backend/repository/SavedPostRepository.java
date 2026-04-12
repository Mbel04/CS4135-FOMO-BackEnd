package com.fomo.backend.repository;

import com.fomo.backend.entity.Post;
import com.fomo.backend.entity.SavedPost;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, UUID> {
    Optional<SavedPost> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    List<SavedPost> findByUser(User user);
}
