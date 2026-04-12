package com.fomo.backend.service;

import com.fomo.backend.dto.response.FriendRequestResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.entity.*;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final BlockRepository blockRepository;
    private final NotificationService notificationService;

    @Transactional
    public void sendFriendRequest(String email, UUID targetUserId) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Cannot send friend request to yourself");
        }
        if (blockRepository.existsBlockBetween(sender, receiver)) {
            throw new ForbiddenException("Cannot send friend request to a blocked user");
        }
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new BadRequestException("Friend request already sent");
        }
        if (friendshipRepository.existsByUsers(sender, receiver)) {
            throw new BadRequestException("Already friends");
        }

        FriendRequest request = FriendRequest.builder().sender(sender).receiver(receiver).build();
        friendRequestRepository.save(request);

        notificationService.createNotification(receiver, Notification.NotificationType.FRIEND_REQUEST,
                sender.getUsername() + " sent you a friend request", sender.getId());
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getIncomingRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequest.Status.PENDING).stream()
                .map(FriendRequestResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptRequest(String email, UUID requestId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (!request.getReceiver().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your friend request");
        }
        request.setStatus(FriendRequest.Status.ACCEPTED);
        friendRequestRepository.save(request);

        Friendship friendship = Friendship.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void declineRequest(String email, UUID requestId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        if (!request.getReceiver().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your friend request");
        }
        request.setStatus(FriendRequest.Status.DECLINED);
        friendRequestRepository.save(request);
    }

    @Transactional
    public void removeFriend(String email, UUID friendId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        Friendship friendship = friendshipRepository.findByUsers(user, friend)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));
        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFriends(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return friendshipRepository.findAllByUser(user).stream()
                .map(f -> f.getUser1().getId().equals(user.getId()) ? f.getUser2() : f.getUser1())
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
