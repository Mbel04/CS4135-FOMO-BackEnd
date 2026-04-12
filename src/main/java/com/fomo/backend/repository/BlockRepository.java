package com.fomo.backend.repository;

import com.fomo.backend.entity.Block;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    List<Block> findByBlocker(User blocker);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END FROM Block b " +
           "WHERE (b.blocker = :u1 AND b.blocked = :u2) OR (b.blocker = :u2 AND b.blocked = :u1)")
    boolean existsBlockBetween(@Param("u1") User u1, @Param("u2") User u2);
}
