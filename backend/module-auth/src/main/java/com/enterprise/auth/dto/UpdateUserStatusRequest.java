package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @file UpdateUserStatusRequest.java
 * @description 更新使用者狀態請求 DTO / Update user status request DTO
 * @description_en Carries the target account status such as ACTIVE or INACTIVE
 * @description_zh 承載目標帳號狀態，例如 ACTIVE 或 INACTIVE
 */
public record UpdateUserStatusRequest(
        @NotBlank(message = "Status cannot be blank")
        String status
) {
}
