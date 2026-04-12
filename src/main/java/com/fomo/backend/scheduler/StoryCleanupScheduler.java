package com.fomo.backend.scheduler;

import com.fomo.backend.service.StoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryCleanupScheduler {

    private final StoryService storyService;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredStories() {
        log.info("Running expired story cleanup...");
        storyService.deleteExpiredStories();
        log.info("Expired story cleanup complete.");
    }
}
