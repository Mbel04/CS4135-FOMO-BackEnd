package com.fomo.backend.service;

import com.fomo.backend.dto.request.CreateGroupChatRequest;
import com.fomo.backend.dto.request.SendGroupMessageRequest;
import com.fomo.backend.dto.request.UpdateGroupChatRequest;
import com.fomo.backend.dto.response.GroupChatResponse;
import com.fomo.backend.dto.response.GroupMessageResponse;
import com.fomo.backend.entity.*;
import com.fomo.backend.exception.BadRequestException;
import com.fomo.backend.exception.ForbiddenException;
import com.fomo.backend.exception.ResourceNotFoundException;
import com.fomo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public GroupChatResponse createGroupChat(String email, CreateGroupChatRequest request) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
        members.add(creator);

        GroupChat groupChat = GroupChat.builder()
                .name(request.getName())
                .creator(creator)
                .members(members)
                .build();
        groupChat = groupChatRepository.save(groupChat);

        if (request.isInitialMessage() && request.getMessageContent() != null) {
            GroupMessage msg = GroupMessage.builder()
                    .groupChat(groupChat)
                    .sender(creator)
                    .content(request.getMessageContent())
                    .build();
            groupMessageRepository.save(msg);
        }

        return GroupChatResponse.from(groupChat);
    }

    @Transactional(readOnly = true)
    public List<GroupChatResponse> getGroupChats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return groupChatRepository.findByMember(user).stream()
                .map(GroupChatResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupMessageResponse> getGroupMessages(String email, UUID groupId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(user, groupChat);
        return groupMessageRepository.findByGroupChatOrderByCreatedAtAsc(groupChat).stream()
                .map(GroupMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupMessageResponse sendGroupMessage(String email, UUID groupId, SendGroupMessageRequest request) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(sender, groupChat);

        GroupMessage message = GroupMessage.builder()
                .groupChat(groupChat)
                .sender(sender)
                .content(request.getMessageContent())
                .sharedPostId(request.getSharedPostId())
                .build();
        message = groupMessageRepository.save(message);

        groupChat.getMembers().stream()
                .filter(m -> !m.getId().equals(sender.getId()))
                .forEach(m -> notificationService.createNotification(m,
                        Notification.NotificationType.MESSAGE,
                        sender.getUsername() + " sent a message in " + groupChat.getName(),
                        groupChat.getId()));

        return GroupMessageResponse.from(message);
    }

    @Transactional
    public GroupChatResponse addMember(String email, UUID groupId, UUID userId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(user, groupChat);

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User to add not found"));

        if (groupChat.getMembers().stream().anyMatch(m -> m.getId().equals(userId))) {
            throw new BadRequestException("User is already a member");
        }
        groupChat.getMembers().add(newMember);
        return GroupChatResponse.from(groupChatRepository.save(groupChat));
    }

    @Transactional
    public void removeMember(String email, UUID groupId, UUID userId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(user, groupChat);

        groupChat.getMembers().removeIf(m -> m.getId().equals(userId));
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public void leaveGroupChat(String email, UUID groupId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(user, groupChat);

        groupChat.getMembers().removeIf(m -> m.getId().equals(user.getId()));
        groupChatRepository.save(groupChat);
    }

    @Transactional
    public GroupChatResponse updateGroupChat(String email, UUID groupId, UpdateGroupChatRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found"));
        checkMember(user, groupChat);
        groupChat.setName(request.getNewName());
        return GroupChatResponse.from(groupChatRepository.save(groupChat));
    }

    private void checkMember(User user, GroupChat groupChat) {
        boolean isMember = groupChat.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) {
            throw new ForbiddenException("You are not a member of this group chat");
        }
    }
}
