package com.fomo.backend.service;

import com.fomo.backend.config.SupabaseStorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseStorageProperties props;

    @PostConstruct
    public void init() {
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
                throw new RuntimeException("Storage upload failed (" + status + "): " + response);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    public void deleteFile(String bucket, String storagePath) {
        if (storagePath == null || storagePath.isBlank()) return;
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
}
