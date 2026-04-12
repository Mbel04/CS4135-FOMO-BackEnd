package com.fomo.backend.service;

import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.entity.Block;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.BlockRepository;
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
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public void blockUser(String email, UUID targetUserId) {
        User blocker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (blocker.getId().equals(blocked.getId())) {
            throw new BadRequestException("Cannot block yourself");
        }
        if (blockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new BadRequestException("User is already blocked");
        }

        friendshipRepository.findByUsers(blocker, blocked)
                .ifPresent(friendshipRepository::delete);

        blockRepository.save(Block.builder().blocker(blocker).blocked(blocked).build());
    }

    @Transactional
    public void unblockUser(String email, UUID targetUserId) {
        User blocker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        Block block = blockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new BadRequestException("User is not blocked"));
        blockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getBlockedUsers(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return blockRepository.findByBlocker(user).stream()
                .map(Block::getBlocked)
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
