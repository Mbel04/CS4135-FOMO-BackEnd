package com.fomo.backend.repository;

import com.fomo.backend.entity.Conversation;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p = :user")
    List<Conversation> findByParticipant(@Param("user") User user);

    @Query("SELECT c FROM Conversation c WHERE :u1 MEMBER OF c.participants AND :u2 MEMBER OF c.participants AND SIZE(c.participants) = 2")
    Optional<Conversation> findDirectConversation(@Param("u1") User u1, @Param("u2") User u2);
}
