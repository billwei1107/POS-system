package com.enterprise.auth.dto;

import com.enterprise.auth.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @file UserResponse.java
 * @description 使用者安全回應 DTO / Safe user response DTO
 * @description_en Exposes user account metadata without password hashes
 * @description_zh 回傳使用者帳號資訊，但不包含密碼雜湊等敏感欄位
 */
public record UserResponse(
        UUID id,
        String username,
        String email,
        String phone,
        String status,
        Integer failedAttempts,
        LocalDateTime lockedUntil,
        Boolean mfaEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getFailedAttempts(),
                user.getLockedUntil(),
                user.getMfaEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
