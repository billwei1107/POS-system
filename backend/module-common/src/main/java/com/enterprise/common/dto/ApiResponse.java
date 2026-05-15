package com.enterprise.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * @file ApiResponse.java
 * @description API 統一回應格式 / Standardized API response format
 * @description_en Wraps all API responses with standard properties like code, message, and data
 * @description_zh 封裝所有 API 回傳資料，統一代碼、訊息格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private static final ZoneId API_TIME_ZONE = ZoneId.of("Asia/Taipei");

    private int code;
    private String message;
    private T data;
    private OffsetDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, now());
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, now());
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(API_TIME_ZONE);
    }
}
