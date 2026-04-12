package com.fomo.backend.repository;

import com.fomo.backend.entity.NotificationSettings;
import com.fomo.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, UUID> {
    Optional<NotificationSettings> findByUser(User user);
}
