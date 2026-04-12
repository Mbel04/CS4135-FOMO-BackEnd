package com.fomo.backend.service;

import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.entity.CloseFriend;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.CloseFriendRepository;
import com.fomo.backend.repository.FriendshipRepository;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloseFriendService {

    private final CloseFriendRepository closeFriendRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public void addCloseFriend(String email, UUID targetUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (!friendshipRepository.existsByUsers(user, target)) {
            throw new BadRequestException("You must be friends before adding as close friend");
        }
        if (closeFriendRepository.existsByUserAndCloseFriend(user, target)) {
            throw new BadRequestException("Already a close friend");
        }
        closeFriendRepository.save(CloseFriend.builder().user(user).closeFriend(target).build());
    }

    @Transactional
    public void removeCloseFriend(String email, UUID targetUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        CloseFriend cf = closeFriendRepository.findByUserAndCloseFriend(user, target)
                .orElseThrow(() -> new ResourceNotFoundException("Close friend not found"));
        closeFriendRepository.delete(cf);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getCloseFriends(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return closeFriendRepository.findByUser(user).stream()
                .map(CloseFriend::getCloseFriend)
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
