package com.fomo.backend.service;

import com.fomo.backend.dto.request.NotificationSettingsRequest;
import com.fomo.backend.dto.response.NotificationResponse;
import com.fomo.backend.entity.Notification;
import com.fomo.backend.entity.NotificationSettings;
import com.fomo.backend.entity.User;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.ConversationRepository;
import com.fomo.backend.repository.GroupChatRepository;
import com.fomo.backend.repository.NotificationRepository;
import com.fomo.backend.repository.NotificationSettingsRepository;
import com.fomo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final GroupChatRepository groupChatRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public void createNotification(User user, Notification.NotificationType type, String message, UUID referenceId) {
        NotificationSettings settings = settingsRepository.findByUser(user).orElse(null);
        if (settings != null && !isTypeEnabled(settings, type)) {
            return;
        }
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);
    }

    private boolean isTypeEnabled(NotificationSettings s, Notification.NotificationType type) {
        return switch (type) {
            case LIKE -> s.isLikes();
            case FRIEND_REQUEST -> s.isFriendRequests();
            case TAG -> s.isTags();
            case MESSAGE, GROUP_MESSAGE -> s.isMessages();
            case STORY -> s.isStories();
        };
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = NotificationResponse.from(n);
        r.setReferenceKind(resolveReferenceKind(n));
        return r;
    }

    /**
     * Tells the client how to interpret {@code referenceId} for navigation (not stored in DB).
     */
    private String resolveReferenceKind(Notification n) {
        UUID ref = n.getReferenceId();
        if (ref == null) {
            return "none";
        }
        return switch (n.getType()) {
            case LIKE, TAG -> "post";
            case FRIEND_REQUEST -> "user";
            case STORY -> "story";
            case GROUP_MESSAGE -> "group";
            case MESSAGE -> {
                if (groupChatRepository.existsById(ref)) {
                    yield "group";
                }
                if (conversationRepository.existsById(ref)) {
                    yield "conversation";
                }
                yield "conversation";
            }
        };
    }

    @Transactional
    public void markAsRead(String email, UUID notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Notification> unread = notificationRepository.findByUserAndReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(String email, UUID notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your notification");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void updateSettings(String email, NotificationSettingsRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        NotificationSettings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> NotificationSettings.builder().user(user).build());
        settings.setLikes(request.isLikes());
        settings.setFriendRequests(request.isFriendRequests());
        settings.setTags(request.isTags());
        settings.setMessages(request.isMessages());
        settings.setStories(request.isStories());
        settingsRepository.save(settings);
    }
}
