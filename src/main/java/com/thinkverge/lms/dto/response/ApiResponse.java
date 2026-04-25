package com.thinkverge.lms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 *
 * Usage:
 *   ApiResponse<String>         → for plain text/AI replies
 *   ApiResponse<UserResponse>   → for object payloads
 *   ApiResponse<Void>           → for success/error with no data
 *
 * The old non-generic ApiResponse had only {success, message}.
 * This version adds a generic <T> data field so controllers can
 * return ApiResponse<String>, ApiResponse<List<X>>, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // ── Convenience factory methods ───────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}