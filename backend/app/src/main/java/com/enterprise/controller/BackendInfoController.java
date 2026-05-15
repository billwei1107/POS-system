package com.enterprise.controller;

import com.enterprise.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * @file BackendInfoController.java
 * @description 後端入口資訊 Controller / Backend entry information controller
 * @description_en Provides a public backend root response for local development checks
 * @description_zh 提供公開的後端根路徑回應，避免本地開發時誤判後端無法進入
 */
@RestController
public class BackendInfoController {
    private static final ZoneId POS_TIME_ZONE = ZoneId.of("Asia/Taipei");

    // ========================================
    // 後端根路徑資訊 / Backend Root Information
    // ========================================
    @GetMapping({"/", "/api"})
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.success(Map.of(
                "service", "Titanium POS Backend",
                "status", "UP",
                "health", "/actuator/health",
                "apiBase", "/api/v1",
                "frontend", "http://127.0.0.1:38182",
                "timezone", POS_TIME_ZONE.toString(),
                "serverTime", OffsetDateTime.now(POS_TIME_ZONE).toString()
        ));
    }
}
