package com.fomo.backend.service;

import com.fomo.backend.dto.request.LoginRequest;
import com.fomo.backend.dto.request.RegisterRequest;
import com.fomo.backend.dto.response.AuthResponse;
import com.fomo.backend.dto.response.UserResponse;
import com.fomo.backend.entity.NotificationSettings;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.repository.NotificationSettingsRepository;
import com.fomo.backend.repository.UserRepository;
import com.fomo.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        NotificationSettings settings = NotificationSettings.builder().user(user).build();
        notificationSettingsRepository.save(settings);

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.isBanned()) {
            throw new BadRequestException("Your account has been banned");
        }

        String token = tokenProvider.generateToken(auth);
        return new AuthResponse(token, UserResponse.from(user));
    }
}
