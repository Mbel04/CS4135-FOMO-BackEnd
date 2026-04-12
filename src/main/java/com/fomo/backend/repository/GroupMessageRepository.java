package com.fomo.backend.repository;

import com.fomo.backend.entity.GroupChat;
import com.fomo.backend.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, UUID> {
    List<GroupMessage> findByGroupChatOrderByCreatedAtAsc(GroupChat groupChat);
}
