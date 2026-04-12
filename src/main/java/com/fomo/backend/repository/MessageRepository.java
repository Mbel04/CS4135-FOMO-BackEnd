package com.fomo.backend.repository;

import com.fomo.backend.entity.Conversation;
import com.fomo.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    List<Message> findByConversationAndReadAtIsNull(Conversation conversation);
}
