package com.fomo.backend.repository;

import com.fomo.backend.entity.GroupChat;
import com.fomo.backend.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, UUID> {

    @Query("SELECT m FROM GroupMessage m JOIN FETCH m.sender JOIN FETCH m.groupChat "
            + "WHERE m.groupChat = :groupChat ORDER BY m.createdAt ASC")
    List<GroupMessage> findByGroupChatOrderByCreatedAtAsc(@Param("groupChat") GroupChat groupChat);
}
