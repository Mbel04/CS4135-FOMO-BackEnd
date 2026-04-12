package com.fomo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FomoBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FomoBackendApplication.class, args);
    }
}
