package com.fomo.backend.repository;

import com.fomo.backend.entity.CloseFriend;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CloseFriendRepository extends JpaRepository<CloseFriend, UUID> {
    Optional<CloseFriend> findByUserAndCloseFriend(User user, User closeFriend);
    boolean existsByUserAndCloseFriend(User user, User closeFriend);
    List<CloseFriend> findByUser(User user);
}
