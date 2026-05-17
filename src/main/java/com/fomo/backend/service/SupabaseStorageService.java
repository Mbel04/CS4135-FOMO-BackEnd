package com.fomo.backend.service;

import com.fomo.backend.config.SupabaseStorageProperties;
import com.fomo.backend.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseStorageProperties props;

    @Value("${fomo.local-media-dir:local-uploads}")
    private String localMediaDir;

    private Path localMediaRoot;

    private boolean isConfigured() {
        return props.getUrl() != null && !props.getUrl().isBlank()
                && props.getServiceRoleKey() != null && !props.getServiceRoleKey().isBlank();
    }

    @PostConstruct
    public void init() {
        if (!isConfigured()) {
            localMediaRoot = Paths.get(localMediaDir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(localMediaRoot);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot create local media directory: " + localMediaRoot, e);
            }
            log.warn("Supabase not configured; media files are stored under {} (local dev).", localMediaRoot);
            return;
        }
        String key = props.getServiceRoleKey().trim();
        log.info("Supabase key length={}, starts={}, ends={}",
                key.length(), key.substring(0, Math.min(20, key.length())),
                key.substring(Math.max(0, key.length() - 10)));
        testConnection(key);
        ensureBucketExists(SupabaseStorageProperties.POSTS_BUCKET);
        ensureBucketExists(SupabaseStorageProperties.STORIES_BUCKET);
    }

    private void testConnection(String key) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    props.getUrl() + "/storage/v1/bucket").openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + key);
            conn.setRequestProperty("apikey", key);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            int status = conn.getResponseCode();
            String body = readResponse(conn, status);
            log.info("Supabase Storage connectivity test: status={}, body={}", status, body);
            conn.disconnect();
        } catch (Exception e) {
            log.warn("Supabase Storage connectivity test failed: {}", e.getMessage());
        }
    }

    private void ensureBucketExists(String bucketName) {
        String key = props.getServiceRoleKey().trim();
        String body = "{\"id\":\"" + bucketName + "\",\"name\":\"" + bucketName + "\",\"public\":true}";

        try {
            HttpURLConnection conn = openConnection(
                    props.getUrl() + "/storage/v1/bucket", "POST", "application/json", key);
            writeBody(conn, body.getBytes(StandardCharsets.UTF_8));
            int status = conn.getResponseCode();

            if (status == 200 || status == 201) {
                log.info("Created Supabase Storage bucket: {}", bucketName);
            } else {
                String response = readResponse(conn, status);
                if (response.contains("already exists") || response.contains("Duplicate")
                        || status == 409 || status == 422) {
                    log.info("Supabase Storage bucket already exists: {}", bucketName);
                } else {
                    log.warn("Could not auto-create bucket {} (create it manually): {} - {}",
                            bucketName, status, response);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            log.warn("Could not auto-create bucket {}: {}", bucketName, e.getMessage());
        }
    }

    public String uploadFile(String bucket, String folder, MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        String storagePath = folder + "/" + UUID.randomUUID() + extension;

        if (!isConfigured()) {
            try {
                Path dir = localMediaRoot.resolve(bucket).resolve(folder);
                Files.createDirectories(dir);
                String fileName = storagePath.substring(storagePath.lastIndexOf('/') + 1);
                Path target = dir.resolve(fileName);
                Files.write(target, file.getBytes());
                log.info("Saved upload locally: {}", target);
                return storagePath;
            } catch (IOException e) {
                log.error("Local media save failed: {}", e.getMessage());
                throw new BadRequestException("Could not save media file locally. Check disk permissions or path.");
            }
        }

        String key = props.getServiceRoleKey().trim();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        try {
            byte[] bytes = file.getBytes();
            String url = props.getUrl() + "/storage/v1/object/" + bucket + "/" + storagePath;

            HttpURLConnection conn = openConnection(url, "POST", contentType, key);
            conn.setRequestProperty("x-upsert", "true");
            writeBody(conn, bytes);
            int status = conn.getResponseCode();

            if (status == 200 || status == 201) {
                log.info("Uploaded file to {}/{}", bucket, storagePath);
                return storagePath;
            } else {
                String response = readResponse(conn, status);
                log.error("Supabase Storage upload failed ({}) for {}: {}", status, storagePath, response);
                throw new BadRequestException(
                        "Media upload failed (" + status + "). " + truncateForClient(response));
            }
        } catch (IOException e) {
            log.error("Upload IO error: {}", e.getMessage());
            throw new BadRequestException("Could not read or send the uploaded file.");
        }
    }

    public void deleteFile(String bucket, String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return;

        if (!isConfigured()) {
            try {
                Path base = localMediaRoot.resolve(bucket).normalize();
                Path file = base.resolve(storagePath).normalize();
                if (file.startsWith(base)) {
                    Files.deleteIfExists(file);
                }
            } catch (IOException e) {
                log.warn("Failed to delete local file {}/{}: {}", bucket, storagePath, e.getMessage());
            }
            return;
        }

        String key = props.getServiceRoleKey().trim();

        try {
            HttpURLConnection conn = openConnection(
                    props.getUrl() + "/storage/v1/object/" + bucket + "/" + storagePath,
                    "DELETE", null, key);
            conn.setFixedLengthStreamingMode(0);
            conn.connect();
            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("Delete file {}/{} returned {}", bucket, storagePath, status);
            }
            conn.disconnect();
        } catch (Exception e) {
            log.warn("Failed to delete file {}/{}: {}", bucket, storagePath, e.getMessage());
        }
    }

    public String getPublicUrl(String bucket, String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return null;
        if (!isConfigured()) {
            return "/api/v1/local-media/" + bucket + "/" + storagePath;
        }
        return props.getUrl() + "/storage/v1/object/public/" + bucket + "/" + storagePath;
    }

    public String detectMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return "image";
        return contentType.startsWith("video/") ? "video" : "image";
    }

    private HttpURLConnection openConnection(String urlStr, String method, String contentType, String key)
            throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + key);
        conn.setRequestProperty("apikey", key);
        if (contentType != null) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        conn.setDoOutput("POST".equals(method) || "PUT".equals(method));
        conn.setDoInput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        return conn;
    }

    private void writeBody(HttpURLConnection conn, byte[] body) throws IOException {
        conn.setFixedLengthStreamingMode(body.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }
    }

    private String readResponse(HttpURLConnection conn, int status) {
        try {
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) return "(no response body)";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "(failed to read response)";
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private static String truncateForClient(String response) {
        if (response == null || response.isBlank()) return "Check storage configuration and bucket permissions.";
        String t = response.replace('\n', ' ').trim();
        return t.length() > 200 ? t.substring(0, 200) + "…" : t;
    }
}
