package com.fomo.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseStorageProperties {
    private String url;
    private String serviceRoleKey;

    public static final String POSTS_BUCKET = "posts-media";
    public static final String STORIES_BUCKET = "stories-media";
}
