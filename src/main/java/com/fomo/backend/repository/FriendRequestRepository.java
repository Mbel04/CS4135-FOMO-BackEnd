package com.fomo.backend.repository;

import com.fomo.backend.entity.FriendRequest;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    boolean existsBySenderAndReceiver(User sender, User receiver);
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);
}
