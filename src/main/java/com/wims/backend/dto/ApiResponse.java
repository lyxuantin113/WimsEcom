package com.wims.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T result) {
    public ApiResponse {
        if (code == 0)
            code = 1000;
    }

    public static <T> ApiResponseBuilder<T> success(T result) {
        return ApiResponse.<T>builder().code(1000).message("Success").result(result);
    }

    public static <T> ApiResponseBuilder<T> error(int code, String message) {
        return ApiResponse.<T>builder().code(code).message(message);
    }
}