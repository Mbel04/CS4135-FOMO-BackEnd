package com.fomo.backend.repository;

import com.fomo.backend.entity.Category;
import com.fomo.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByCategoriesContaining(Category category);

    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.name ILIKE %:categoryName%")
    List<Post> findByCategoryName(@Param("categoryName") String categoryName);
}
