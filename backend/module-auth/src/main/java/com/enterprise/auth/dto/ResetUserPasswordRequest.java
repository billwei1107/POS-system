package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @file ResetUserPasswordRequest.java
 * @description 重設使用者密碼請求 DTO / Reset user password request DTO
 * @description_en Carries the new password used by account administrators
 * @description_zh 承載帳號管理者設定的新密碼
 */
public record ResetUserPasswordRequest(
        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
