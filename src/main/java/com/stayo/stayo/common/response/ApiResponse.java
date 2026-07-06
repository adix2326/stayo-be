package com.stayo.stayo.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Default success (Status 200)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200) // Default HTTP OK
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    // Success with custom status code (e.g., 201 Created)
    public static <T> ApiResponse<T> success(int status, T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    // Error with custom status code (e.g., 400 Bad Request, 404 Not Found)
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
