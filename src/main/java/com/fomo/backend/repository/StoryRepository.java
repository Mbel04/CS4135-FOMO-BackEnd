package com.fomo.backend.repository;

import com.fomo.backend.entity.Story;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {
    @Query("SELECT s FROM Story s WHERE s.user IN :users AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesForUsers(@Param("users") List<User> users, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.expiresAt <= :now")
    List<Story> findExpiredStories(@Param("now") LocalDateTime now);

    List<Story> findByUser(User user);
}
