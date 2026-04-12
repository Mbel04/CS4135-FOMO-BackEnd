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
    @Query("SELECT g FROM GroupChat g JOIN g.members m WHERE m = :user")
    List<GroupChat> findByMember(@Param("user") User user);
}
