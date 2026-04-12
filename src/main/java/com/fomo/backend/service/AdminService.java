package com.fomo.backend.service;

import com.fomo.backend.entity.User;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void verifyUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void unverifyUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerified(false);
        userRepository.save(user);
    }

    @Transactional
    public void banUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBanned(false);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
