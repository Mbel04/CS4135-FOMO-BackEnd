package com.fomo.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ConfigurationValidator {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @PostConstruct
    public void validate() {
        List<String> missing = new ArrayList<>();

        if (!StringUtils.hasText(datasourceUrl) || datasourceUrl.contains("<"))
            missing.add("DB_URL");
        if (!StringUtils.hasText(datasourceUsername) || datasourceUsername.contains("<"))
            missing.add("DB_USERNAME");
        if (!StringUtils.hasText(datasourcePassword) || datasourcePassword.contains("<"))
            missing.add("DB_PASSWORD");
        if (!StringUtils.hasText(jwtSecret) || jwtSecret.contains("<"))
            missing.add("JWT_SECRET");
        if (!StringUtils.hasText(supabaseUrl) || supabaseUrl.contains("<"))
            missing.add("SUPABASE_URL");
        if (!StringUtils.hasText(supabaseServiceRoleKey) || supabaseServiceRoleKey.contains("<"))
            missing.add("SUPABASE_SERVICE_ROLE_KEY");

        if (!missing.isEmpty()) {
            String message = "Application startup aborted. The following required environment variables are not set: "
                    + String.join(", ", missing)
                    + ". See .env.example for the full list.";
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("Configuration validation passed – all required environment variables are present.");
    }
}
