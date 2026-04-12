package com.fomo.backend.repository;

import com.fomo.backend.entity.Like;
import com.fomo.backend.entity.Post;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    long countByPost(Post post);
}
