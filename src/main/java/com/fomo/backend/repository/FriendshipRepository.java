package com.fomo.backend.repository;

import com.fomo.backend.entity.Friendship;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user OR f.user2 = :user)")
    List<Friendship> findAllByUser(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    Optional<Friendship> findByUsers(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM Friendship f " +
           "WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    boolean existsByUsers(@Param("u1") User u1, @Param("u2") User u2);
}
