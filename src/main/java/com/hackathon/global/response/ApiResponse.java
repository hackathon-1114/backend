package com.hackathon.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 API 공통 응답 포맷.
 * { "success": true, "code": "OK", "message": "...", "data": {...} }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "성공", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "OK", "성공", null);
    }

    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
