package com.enterprise.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

/**
 * @file CreateUserRequest.java
 * @description 建立後台帳號請求 DTO / Create admin user request DTO
 * @description_en Carries account credentials, contact fields, and initial roles
 * @description_zh 承載帳號密碼、聯絡欄位與初始角色指派
 */
public record CreateUserRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,
        @NotBlank(message = "Password cannot be blank")
        String password,
        String email,
        String phone,
        List<UUID> roleIds
) {
}
