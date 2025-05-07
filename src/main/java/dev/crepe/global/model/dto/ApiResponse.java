package dev.crepe.global.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private final String status;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .status("fail")
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> ApiResponse<T> processing(String message, T data) {
        return ApiResponse.<T>builder()
                .status("processing")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}