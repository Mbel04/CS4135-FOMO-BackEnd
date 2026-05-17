package com.fomo.backend.repository;

import com.fomo.backend.entity.GroupChat;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, UUID> {
    @Query("SELECT DISTINCT g FROM GroupChat g JOIN FETCH g.creator JOIN FETCH g.members "
            + "WHERE :user MEMBER OF g.members ORDER BY g.createdAt DESC")
    List<GroupChat> findByMember(@Param("user") User user);
}
