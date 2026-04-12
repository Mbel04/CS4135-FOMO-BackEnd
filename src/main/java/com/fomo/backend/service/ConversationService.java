package com.fomo.backend.service;

import com.fomo.backend.dto.request.SendMessageRequest;
import com.fomo.backend.dto.response.ConversationResponse;
import com.fomo.backend.dto.response.MessageResponse;
import com.fomo.backend.entity.*;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final NotificationService notificationService;

    @Transactional
    public ConversationResponse getOrCreateDirectConversation(String email, UUID targetUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (blockRepository.existsBlockBetween(user, target)) {
            throw new ForbiddenException("Cannot message a blocked user");
        }

        return conversationRepository.findDirectConversation(user, target)
                .map(ConversationResponse::from)
                .orElseGet(() -> {
                    Conversation conv = Conversation.builder()
                            .participants(Set.of(user, target))
                            .build();
                    return ConversationResponse.from(conversationRepository.save(conv));
                });
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return conversationRepository.findByParticipant(user).stream()
                .map(ConversationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(String email, UUID conversationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        checkParticipant(user, conversation);
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse sendMessage(String email, UUID conversationId, SendMessageRequest request) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        checkParticipant(sender, conversation);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getMessageContent())
                .sharedPostId(request.getSharedPostId())
                .build();
        message = messageRepository.save(message);

        conversation.getParticipants().stream()
                .filter(p -> !p.getId().equals(sender.getId()))
                .forEach(p -> notificationService.createNotification(p,
                        Notification.NotificationType.MESSAGE,
                        sender.getUsername() + " sent you a message",
                        conversation.getId()));

        return MessageResponse.from(message);
    }

    @Transactional
    public void markConversationAsRead(String email, UUID conversationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        checkParticipant(user, conversation);

        List<Message> unread = messageRepository.findByConversationAndReadAtIsNull(conversation).stream()
                .filter(m -> !m.getSender().getId().equals(user.getId()))
                .collect(Collectors.toList());
        unread.forEach(m -> m.setReadAt(LocalDateTime.now()));
        messageRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessageThread(String email, UUID otherUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findDirectConversation(user, other)
                .orElseThrow(() -> new ResourceNotFoundException("No conversation found"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    private void checkParticipant(User user, Conversation conversation) {
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(user.getId()));
        if (!isParticipant) {
            throw new ForbiddenException("You are not part of this conversation");
        }
    }
}
