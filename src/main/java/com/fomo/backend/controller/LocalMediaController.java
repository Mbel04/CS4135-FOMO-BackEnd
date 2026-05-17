package com.fomo.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class LocalMediaController {

    @Value("${fomo.local-media-dir:local-uploads}")
    private String localMediaDir;

    @GetMapping("/api/v1/local-media/{bucket}/**")
    public ResponseEntity<Resource> getLocalMedia(@PathVariable String bucket, HttpServletRequest request)
            throws IOException {
        String pathWithin = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (pathWithin == null || pathWithin.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path base = Paths.get(localMediaDir).toAbsolutePath().normalize().resolve(bucket);
        Path file = base.resolve(pathWithin).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String probe = Files.probeContentType(file);
        MediaType mediaType = MediaType.parseMediaType(probe != null ? probe : "application/octet-stream");
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
