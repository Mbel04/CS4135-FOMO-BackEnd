package com.fomo.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }
}
